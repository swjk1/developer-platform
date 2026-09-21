"use client";

import { useCallback, useRef } from "react";
import { ArrowRight, CloseIcon } from "./icons";

/**
 * Uses the native <dialog> element rather than a hand-rolled overlay: it gets
 * focus trapping, Escape-to-close and inert backgrounding from the platform.
 * The only thing added here is dismissing on a backdrop click.
 */
export function ArchitectureDialog() {
  const ref = useRef<HTMLDialogElement>(null);

  // No `disabled until mounted` guard here on purpose. Rendering the button
  // disabled on the server and enabled on the client is itself a hydration
  // mismatch, and it leaves the button dead for the first moments after load —
  // exactly when an impatient visitor clicks it. The optional call below is
  // enough: before hydration the click simply does nothing.
  const open = useCallback(() => ref.current?.showModal(), []);
  const close = useCallback(() => ref.current?.close(), []);

  // A click whose target is the dialog itself landed on the backdrop, since
  // every real child sits inside .modal__bar or .modal__body.
  const onBackdrop = useCallback((event: React.MouseEvent<HTMLDialogElement>) => {
    if (event.target === ref.current) close();
  }, [close]);

  return (
    <>
      <button className="btn btn--ghost" type="button" onClick={open}>
        Architecture <ArrowRight />
      </button>

      <dialog className="modal" ref={ref} onClick={onBackdrop} aria-labelledby="arch-title">
        <div className="modal__bar">
          <h3 id="arch-title">Navigation Assistant — Architecture</h3>
          <button className="modal__x" type="button" aria-label="Close" onClick={close}>
            <CloseIcon />
          </button>
        </div>

        <div className="modal__body">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src="/diagrams/architecture.svg"
            alt="Architecture. A single ARCore session feeds two consumers: the RGB frame goes to on-device YOLO and ML Kit OCR, optionally enriched by a gated Gemini vision model, then mapped to semantic observations; pose and depth go to the navigation runtime and into a pure Kotlin engine that builds an occupancy grid, inflates obstacles, picks frontiers, plans with A* and a topological map, and emits one instruction. Only compact snapshots cross into JavaScript for speech and haptics."
            width={1000}
            height={930}
          />

          <div className="modal__doc">
            <h4>The camera: one session, two consumers</h4>
            <p>
              ARCore demands exclusive access to the camera, and the engine cannot run without it —
              6DoF pose and depth are its only inputs. So the module that opens the camera has to be
              the one running the session; a second <code>CameraView</code> either fails to open or
              silently evicts the first, with no crash and no error to tell you.
            </p>
            <p>
              <code>indoor-perception</code> therefore owns the single session and both halves read
              the same frame: <b>pose and depth go to navigation, the RGB image goes to
              perception</b>. The RGB is encoded to JPEG only when a capture is actually requested —
              converting every frame at 30 fps would burn battery for nothing.
            </p>

            <h4>Perception: YOLO and OCR, both on the device</h4>
            <p>
              <b>A fine-tuned YOLO26n runs locally through ONNX Runtime</b> — 9.8 MB, 640 × 640
              input, 17 wayfinding classes chosen because COCO has none of the labels that matter
              here. There are no COCO categories for <i>exit sign</i>, <i>elevator sign</i> or{" "}
              <i>push handle</i>, which is why fine-tuning is what makes the semantic half work at
              all.
            </p>
            <p>
              <b>ML Kit OCR reads the text in the same frame</b> — room numbers, floor markers, the
              words on a sign. Together they answer different halves of the same question: YOLO
              finds that there <i>is</i> a door, OCR finds that it says <i>314</i>.
            </p>
            <p>
              Labels are then mapped to their <b>subject rather than flattened to a generic
              sign</b>. An <code>exit sign</code> becomes <code>EXIT</code> plus a direction, a{" "}
              <code>stair sign</code> becomes <code>STAIRS</code>. Calling an exit sign merely
              &ldquo;a sign&rdquo; throws away the only useful part of it. <code>person</code> and{" "}
              <code>trash can</code> are dropped entirely — the engine already sees those through
              depth.
            </p>

            <h4>Gemini is a gate, not a dependency</h4>
            <p>
              A Gemini Flash vision model can be asked for a second opinion on a capture, gated per
              request rather than run on every frame. <b>It is strictly optional.</b> Without a key,
              YOLO and OCR still run entirely on device and only the VLM stage reports{" "}
              <code>mock</code> — mapping and planning have no model dependency at all.
            </p>
            <p>
              Whatever perception produces, it only ever <b>biases</b> frontier choice. The planner
              decides movement. A sign that covers the target counts as strong positive evidence; a
              sign that does not is only weak negative evidence, so an alternative branch is never
              eliminated by a misread.
            </p>

            <h4>Mapping: what the engine actually knows</h4>
            <p>
              Depth folds into an occupancy grid of <b>10 cm cells, 120 × 120 over a 12 m
              window</b>, with readings past 6 m discarded because accuracy degrades badly beyond
              it. Free space is carved by ray-casting rather than marked point by point: the ray
              gains free evidence along its length and occupied evidence at its endpoint, without
              which the map would be a field of obstacle dots with nothing walkable between them.
            </p>
            <p>
              Obstacles are then inflated by body radius, with two deliberate relaxations — the goal
              snaps to the nearest reachable cell when inflation swallowed it, and the path may
              escape the halo <i>around the start</i>, because a person routinely stands closer to a
              wall than their own body radius. Never through a genuinely occupied cell.
            </p>

            <h4>Planning: frontiers, A*, and a graph of where you have walked</h4>
            <p>
              <b>Frontier exploration</b> finds free cells that sit next to unknown ones,
              flood-fills them into clusters, and throws away clusters under six cells as depth
              speckle. Each candidate is scored on information gain and semantic hints against
              distance, revisits and risk — and once chosen, a waypoint is <i>committed to</i>, held
              through frontier splits and merges and released only on arrival, blockage, or twenty
              seconds without getting 0.3 m closer.
            </p>
            <p>
              <b>Local A* plans the route across the grid, then line-of-sight smoothing straightens
              it.</b> Raw grid A* stair-steps, and a stair-stepped path read aloud becomes an
              unusable stream of alternating slight-left, slight-right.
            </p>
            <p>
              <b>A topological map is the building-scale memory</b>, because a 10 cm grid forgets a
              corridor once you are two rooms away. It is a sparse graph of places — nodes created
              every 2.5 m or on a significant turn, never per frame — and a new node within 1.6 m of
              an existing one merges into it, so walking a loop does not lay down duplicate parallel
              corridors. The critical rule: <b>an edge means &ldquo;I have walked this.&rdquo;</b> A
              landmark spotted across a lobby becomes a node but gets no edge to where you were
              standing, because visibility is not walkability — and that missing distinction
              previously let global routing plan a route straight through a wall.
            </p>
            <p>
              <b>Backtracking is graph A* over that memory</b>, answering &ldquo;how do I get back
              to the last junction that still has unexplored ways out&rdquo;. Without it the engine
              would walk someone into the same dead end forever.
            </p>

            <h4>From a path to one spoken instruction</h4>
            <p>
              The controller looks 1.5 m ahead rather than at the next 10 cm cell, smooths heading
              circularly so it does not break at the ±180° seam, and starts a turn at 15° while
              ending it below 8° so the command cannot chatter. A command must hold 350 ms before it
              is spoken and stands 1.4 s once announced — a person needs a beat to hear a turn and
              begin making it. <code>STOP</code> bypasses every one of those gates, because a safety
              stop is never delayed.
            </p>

            <p className="modal__note">
              This describes the source wiring, not a verification run on a physical phone.{" "}
              <a href="https://github.com/swjk1/Navigation-assistant-for-visually-impaired/blob/main/ARCHITECTURE.md">
                Full architecture doc on GitHub →
              </a>
            </p>
          </div>
        </div>
      </dialog>
    </>
  );
}

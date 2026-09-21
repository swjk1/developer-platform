-- Initial portfolio content.
--
-- This is real content rather than test fixtures, which is why it lives in a
-- versioned migration instead of the seed tooling under test-platform/. Once
-- the admin API (phase 6) exists, edits happen through it and this migration
-- stays as the starting state.

INSERT INTO projects (slug, name, short_description, long_description, status, featured, github_url, demo_url, display_order)
VALUES
('navigation-assistant',
 'Navigation Assistant for the Visually Impaired',
 'An Android app that guides a blind or low-vision user through an indoor space it has never seen — no floor plan, no beacons, no installed hardware.',
 'Maps the room with ARCore depth into a 10 cm occupancy grid, reads signs and obstacles on device with a fine-tuned YOLO26n detector and ML Kit OCR, and gives one instruction at a time. The navigation engine is pure Kotlin with no Android SDK or ARCore on its classpath, so it tests on a laptop in milliseconds and an accidental platform import fails the build. Planning combines frontier exploration, local A* with line-of-sight smoothing, and a topological graph whose edges mean "I have walked this".',
 'ACTIVE', TRUE,
 'https://github.com/swjk1/Navigation-assistant-for-visually-impaired',
 NULL, 1),

('keyguard',
 'Keyguard',
 'A safety keyboard and accessibility overlay that warns before a child sends something they shouldn''t. Detection runs on device; nothing typed ever leaves the phone.',
 'A local detection engine does structured PII matching plus a word-boundary-validated lexicon pass over an Aho-Corasick automaton, scanning a 500-character buffer in a median 136 microseconds against a 5 ms budget. Findings carry offsets rather than raw text, so no caller can accidentally log or transmit what was typed. An optional cloud layer only ever refines the local verdict and the keyboard never blocks on it.',
 'ACTIVE', TRUE,
 'https://github.com/swjk1/keyguard',
 NULL, 2),

('cookpilot',
 'CookPilot — Hands-Free Recipe Assistant',
 'A hands-free cooking assistant. Instead of pausing a video with wet hands you talk to it — it has already watched the recipe and reads you one step at a time.',
 'A multimodal extraction pipeline combining Whisper ASR, PaddleOCR over sampled frames, and LLM reasoning to reconcile what is said against what is shown on screen. A keyframe selection step aligns every extracted step with the frame that actually shows it. Deployed on Railway with Docker and PostgreSQL, with voice interrupts and adaptive text-to-speech.',
 'ACTIVE', TRUE,
 'https://github.com/swjk1/CookBot',
 NULL, 3);

INSERT INTO project_technologies (project_id, technology, display_order)
SELECT p.id, t.technology, t.display_order
FROM projects p
JOIN (VALUES
    ('navigation-assistant', 'Kotlin',        0),
    ('navigation-assistant', 'ARCore',        1),
    ('navigation-assistant', 'YOLO',          2),
    ('navigation-assistant', 'ONNX Runtime',  3),
    ('navigation-assistant', 'ML Kit OCR',    4),
    ('navigation-assistant', 'React Native',  5),
    ('keyguard',             'Kotlin',        0),
    ('keyguard',             'PyTorch',       1),
    ('keyguard',             'ONNX Runtime',  2),
    ('keyguard',             'Android',       3),
    ('keyguard',             'Next.js',       4),
    ('cookpilot',            'Python',        0),
    ('cookpilot',            'Whisper',       1),
    ('cookpilot',            'PaddleOCR',     2),
    ('cookpilot',            'PostgreSQL',    3),
    ('cookpilot',            'Docker',        4),
    ('cookpilot',            'Railway',       5)
) AS t(slug, technology, display_order) ON t.slug = p.slug;

// The published, user-facing docs (allowlist). Shared by the sync script and the
// VitePress config so the set and its order are defined exactly once.
//   file  — source markdown under ../docs
//   title — sidebar / nav label
//   group — sidebar section the entry belongs to
//
// Internal material (reviews/, plans, design notes) is deliberately NOT listed
// here and therefore stays GitHub-only, matching the other Fennec projects.
export const GUIDES = [
  { file: 'user-guide.md', title: 'User Guide', group: 'Guides' },
  { file: 'qvt-transformations.md', title: 'QVT Transformations', group: 'Guides' },
  { file: 'ci.md', title: 'CI & Publishing', group: 'Guides' },
];

// Route name for a guide: the file's base name without the .md extension.
// e.g. 'user-guide.md' -> 'user-guide', served at /guides/user-guide.
export function slugFor(file) {
  return file.replace(/^.*\//, '').replace(/\.md$/, '');
}

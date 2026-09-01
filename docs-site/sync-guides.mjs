// Sync the curated, user-facing docs from ../docs into ./docs/guides for VitePress.
//
// Single source of truth stays docs/. Publication is an explicit ALLOWLIST
// (guides.mjs) — internal material (reviews, plans, design notes) is
// deliberately NOT published. Cross-links inside the published docs that point
// at a NON-published file are rewritten to the GitHub blob URL so they keep
// working instead of 404-ing on the site; links between published docs become
// sibling routes. Links are resolved relative to each source file's directory
// so entries in sub-folders keep working.
import { readFileSync, writeFileSync, mkdirSync, rmSync } from 'node:fs';
import { dirname, join, posix } from 'node:path';
import { fileURLToPath } from 'node:url';
import { GUIDES, slugFor } from './guides.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const srcDir = join(here, '..', 'docs'); // repo docs/ — source of truth
const outDir = join(here, 'docs', 'guides'); // VitePress content root

// Branch/ref used for the GitHub blob fallback links (internal docs are browsed
// on GitHub, not published). Passed by CI; defaults to snapshot for local builds.
const branch = process.env.DOCS_BRANCH || 'snapshot';
const blobBase = `https://github.com/eclipse-fennec/model.atlas/blob/${branch}/docs`;

// docs-relative source path (posix) -> route slug, for the published set.
const published = new Map(GUIDES.map((g) => [posix.normalize(g.file), slugFor(g.file)]));

// Resolve a markdown link found in `srcFile` to a docs-relative posix path.
function resolveDocsRel(srcFile, target) {
  const fromDir = posix.dirname(posix.normalize(srcFile));
  return posix.normalize(posix.join(fromDir, target));
}

// Rewrite ](target.md#anchor) links. Published target -> sibling route; anything
// else -> GitHub blob URL. External (http[s]) links are left untouched.
function rewriteLinks(md, srcFile) {
  return md.replace(/\]\(([^)\s#]+\.md)(#[^)\s]*)?\)/g, (whole, target, anchor = '') => {
    if (/^https?:\/\//i.test(target)) return whole;
    const rel = resolveDocsRel(srcFile, target);
    if (published.has(rel)) {
      return `](./${published.get(rel)}${anchor})`;
    }
    return `](${blobBase}/${rel}${anchor})`;
  });
}

rmSync(outDir, { recursive: true, force: true });
mkdirSync(outDir, { recursive: true });

for (const g of GUIDES) {
  const src = join(srcDir, g.file);
  const md = rewriteLinks(readFileSync(src, 'utf8'), g.file);
  const slug = slugFor(g.file);
  writeFileSync(join(outDir, `${slug}.md`), md, 'utf8');
  console.log(`synced ${g.file} -> guides/${slug}.md`);
}

console.log(`Done. ${GUIDES.length} guides (branch=${branch}).`);

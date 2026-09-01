import { defineConfig } from 'vitepress'
import { GUIDES, slugFor } from '../../guides.mjs'

// Per-project docs are served under a versioned sub-path, matching the org
// convention (https://eclipse-fennec.github.io/<repo>/<version>/). The snapshot
// branch publishes to /model.atlas/snapshot/; tagged releases / `latest` get
// added once the first release lands.
const version = process.env.DOCS_BRANCH || 'snapshot'
const base = `/model.atlas/${version}/`

// Canonical published origin. Links that point OUTSIDE the current docs base
// (other doc versions) must be full URLs — VitePress auto-prepends `base` to any
// root-absolute (`/…`) link, which would otherwise double the path. Links to
// pages WITHIN this version stay base-relative (e.g. `/guides/user-guide`).
const SITE = 'https://eclipse-fennec.github.io/model.atlas'

// Version selector. Only `snapshot` is deployed today; keep as data so adding
// `latest` and tagged versions later is a one-liner.
const versions = [{ text: 'snapshot', link: `${SITE}/snapshot/` }]

// Build the sidebar as one section per `group`, preserving the order in which
// groups first appear in GUIDES.
const groupOrder = []
const byGroup = new Map()
for (const g of GUIDES) {
  if (!byGroup.has(g.group)) {
    byGroup.set(g.group, [])
    groupOrder.push(g.group)
  }
  byGroup.get(g.group).push({ text: g.title, link: `/guides/${slugFor(g.file)}` })
}
const sidebarGuides = groupOrder.map((name) => ({
  text: name,
  collapsed: false,
  items: byGroup.get(name),
}))

// Compact nav dropdown: the group headers, each linking to its first page.
const navGuides = groupOrder.map((name) => ({
  text: name,
  link: byGroup.get(name)[0].link,
}))

export default defineConfig({
  title: 'Fennec Model Atlas',
  description:
    'A dynamic EMF model management system — a RESTful API for managing schemas and data objects with multi-tenant scopes, stage-based workflows and pluggable storage.',
  lang: 'en-US',
  base,
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,

  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}fennec-logo.png` }],
    ['meta', { name: 'theme-color', content: '#c0631c' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Fennec Model Atlas' }],
    [
      'meta',
      {
        property: 'og:description',
        content:
          'Dynamic EMF model management — REST API, multi-tenant scopes, stage-based workflows, pluggable storage.',
      },
    ],
  ],

  themeConfig: {
    logo: '/fennec-logo.png',
    siteTitle: 'Fennec Model Atlas',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Docs', items: navGuides },
      { text: `version: ${version}`, items: versions },
    ],

    sidebar: {
      '/guides/': sidebarGuides,
    },

    socialLinks: [{ icon: 'github', link: 'https://github.com/eclipse-fennec/model.atlas' }],

    search: { provider: 'local' },

    editLink: {
      pattern: 'https://github.com/eclipse-fennec/model.atlas/edit/main/docs/:path',
      text: 'Edit this page on GitHub',
    },

    footer: {
      message:
        'Released under the EPL-2.0 License. Eclipse Fennec is part of the Eclipse Foundation.',
      copyright: 'Copyright © Eclipse Foundation and contributors',
    },
  },
})

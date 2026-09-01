---
layout: home

hero:
  name: Fennec Model Atlas
  text: Dynamic EMF model management
  tagline: A RESTful API for managing schemas and data objects at runtime — multi-tenant scopes, stage-based workflows, hierarchical visibility and pluggable storage.
  image:
    src: /fennec-logo.png
    alt: Eclipse Fennec logo
  actions:
    - theme: brand
      text: Read the User Guide
      link: /guides/user-guide
    - theme: alt
      text: CI & Publishing
      link: /guides/ci
    - theme: alt
      text: View on GitHub
      link: https://github.com/eclipse-fennec/model.atlas

features:
  - icon: 🗂️
    title: Scopes & Workflows
    details: Multi-tenant scopes with parent-child hierarchies and configurable stage-based workflows — draft, review, approved, release — with hierarchical schema visibility.
    link: /guides/user-guide#core-concepts
    linkText: Core Concepts
  - icon: 🔌
    title: REST API
    details: Jakarta RS-based endpoints for schema packages, object storage, model conversion, validation and data generation — with Swagger/OpenAPI documentation, ETags and content negotiation.
    link: /guides/user-guide#rest-api
    linkText: REST API
  - icon: 🧱
    title: Pluggable Storage
    details: Interchangeable storage backends behind one service contract — local filesystem, Apicurio Registry, Git, and Lucene-backed search and indexing.
    link: /guides/user-guide#configuration
    linkText: Configuration
  - icon: 🔄
    title: Formats & Transformation
    details: One model, many representations — Ecore, JSON Schema, XSD, UML and JSON/BSON/XLSX exports via Fennec Codec, plus QVT model-to-model transformations.
    link: /guides/user-guide#model-converter-api
    linkText: Model Converter API
---

## About Fennec Model Atlas

Fennec Model Atlas (`org.eclipse.fennec.model.atlas`) is a dynamic EMF model
management system built on **OSGi** and the **Eclipse Modeling Framework**. It
loads Ecore models, JSON Schemas and QVT transformations at runtime, registers
them as OSGi services, and exposes them through a RESTful API with multi-tenant
**scopes**, stage-based **workflows** (draft → review → approved → release) and
hierarchical schema visibility.

Ready-to-run Docker images are published in two variants — file-based storage
and [Apicurio Registry](https://www.apicur.io/registry/)-backed storage — see the
[User Guide](/guides/user-guide#getting-started) to get started. Internal
development notes (plans, reviews, design documents) live in the
[`docs/` folder on GitHub](https://github.com/eclipse-fennec/model.atlas/tree/snapshot/docs).

# Apicurio Registry Storage

This document describes the implementation of the `EObjectApicurioStorageService`, which extends the `AbstractEObjectStorageService` in oder to make it possible to store `EObjects` into the Apicurio Registry.

The REST API documentation for Apicurio we used as reference can be found [here](https://www.apicur.io/registry/docs/apicurio-registry/3.0.x/assets-attachments/registry-rest-api.htm#tag/Versions/operation/getArtifactVersionContent).

We are using **v3.0.x** of the Apicurio Registry, so the base url we are using for our requests is **http://<hostName>:<port>/apis/registry/v3**.

## Current Implementation





- Enviroment variable should be set in apicurio docker to enable artifact deletion (`APICURIO_REST_DELETION_ARTIFACT_ENABLED=true`)

- We can use the search and filter by name, so we could create an artifact with id a combination of name and extension, and as name just the name. So, when we look for artifacts or we remove the artifacts we can search by name and then remove them all. 

- When we search there is query parameter `limit` (default = 20) which sets the number of artifacts to return. So it can be that the `count > limit` and so we do not get all the searched artifacts. This we should keep in mind, maybe configure it to be a larger number just to be sure. (Especially because when we want to delete the artifacts we have to delete them all!)

- When we create an artifact in apicurio, we can set one of these 3 behaviours to the `ifExists` query parameter:

  - `FAIL` (default): the request fails with a 409 if an artifact with the same id already exists;
  - `CREATE_VERSION`: the server creates a new version of the existing artifact and returns it
  - `FIND_OR_CREATE_VERSION`: server returns an existing version that matches the provided content if such a version exists, otherwise it creates a new version

  We have to decide how to proceed here. Do we want to just keep updating the same artifact? Do we want for every update to store a different version? We have a version attribute in the metadata as well. We can have multiple variants of the object (e.g. in json, xmi, etc) but the metadata will be just one. If the version has changed then, maybe we should remove the older version in apicurio and save it again as new?? 

- We need to understand how we can retrieve an artifact. Doing something like `http://localhost:8080/apis/registry/v3/groups/default/artifacts/default:draft:test-id-123.json` only retrieves the metadata. To retrieve the actual content we would need to know the version and at that point in the storage we do not have the `ObjectMetadata` from which to extract the version.
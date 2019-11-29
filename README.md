# Wikidata Editor

For development purpose, self-signed certificates were added to `src/main/resources/keystore/wikidataeditor.p12`; for real-life
usage, these should be changed to valid certs since these do not guarantee the authenticity of the webserver.

Re-generation of the whole file is recommended, password must be set in `src/main/resources/application.properties`.

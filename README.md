**[DEPRECATED: The Anvil build service has been turned off, so this client will no longer work](https://devcenter.heroku.com/changelog-items/613)**

# Janvil
A Java-based library for interacting with the [Anvil build service](https://github.com/ddollar/anvil).

## Example Usage

Create an instance of the client with Heroku your API key

    Janvil janvil = new Janvil("HEROKU_API_KEY");

Create a manifest of representing the contents of a directory

    Manifest manifest = new Manifest(dir);
    manifest.addAll();

Upload the files in the manifest to Anvil and build a Heroku slug. Running this command multiple times only uploads new or changed files.

    String slugUrl = janvil.build(manifest);

Release the slug to a Heroku app

    janvil.release("my-app", slugUrl, "release description");


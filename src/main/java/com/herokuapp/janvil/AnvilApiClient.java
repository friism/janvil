package com.herokuapp.janvil;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.herokuapp.janvil.CurlFormDataContentDisposition.curlize;

/**
 * @author Ryan Brainard
 */
class AnvilApiClient extends AbstractApiClient {

    AnvilApiClient(Janvil.Config config) {
        super(config, "anvil.herokuapp.com");
    }

    public ClientResponse post(Manifest manifest) throws IOException {
        return baseResource
                .path("/manifest")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
    }

    public ClientResponse build(Manifest manifest, Map<String, String> env) throws IOException {
        return build(manifest, env, "");
    }

    public ClientResponse build(Manifest manifest, Map<String,String> env, String buildpack) throws IOException {
        return baseResource
                .path("/manifest/build")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE)
                        .field("env", env, MediaType.APPLICATION_JSON_TYPE)
                        .field("buildpack", buildpack)
                );
    }

    public ClientResponse diff(Manifest manifest) throws IOException {
        return baseResource
                .path("/manifest/diff")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
    }

    public ClientResponse post(File file) throws IOException {
        return baseResource
            .path("/file/" + Manifest.hash(file))
            .type(MediaType.MULTIPART_FORM_DATA_TYPE)
            .post(ClientResponse.class, new FormDataMultiPart()
                    .bodyPart(curlize(new FileDataBodyPart("data", file))));
    }

    public ClientResponse get(String hash) throws IOException {
        return baseResource
                .path("/file/" + hash)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(ClientResponse.class);
    }

    public ClientResponse get(File file) throws IOException {
        return get(Manifest.hash(file));
    }

}
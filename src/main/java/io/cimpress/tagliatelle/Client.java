package io.cimpress.tagliatelle;

/**
 * Hello world!
 *
 */
public class Client
{
    private final String accessToken;
    private String tagliatelleUrl = null;

    public Client(String accessToken) {
        this.accessToken = accessToken;
    }


    public Client(String accessToken, String urlOverride) {
        this(accessToken);
        this.tagliatelleUrl = urlOverride;
    }


    public ClientRequest tag()
    {
        return new ClientRequest(this.accessToken, this.tagliatelleUrl);
    }

}

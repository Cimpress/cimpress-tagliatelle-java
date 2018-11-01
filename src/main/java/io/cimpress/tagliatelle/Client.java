package io.cimpress.tagliatelle;

/**
 * Hello world!
 *
 */
public class Client
{
    private final String accessToken;

    public Client(String accessToken) {
        this.accessToken = accessToken;
    }

    public ClientRequest tag()
    {
        return new ClientRequest(this.accessToken, Operation.TAG);
    }

    public ClientRequest untag()
    {
        return new ClientRequest(this.accessToken, Operation.UNTAG);
    }
}

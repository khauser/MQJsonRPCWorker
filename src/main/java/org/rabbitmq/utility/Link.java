package org.rabbitmq.utility;

public class Link extends org.springframework.hateoas.Link {

    private static final long serialVersionUID = -6675829969140188187L;

    private String title;

    public Link() {
    }

    public Link(String href) {
        this(href, REL_SELF);
    }

    public Link(String href, String rel) {
        this(href, rel, null);
    }

    public Link(String href, String rel, String title) {
        super(href, rel);
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

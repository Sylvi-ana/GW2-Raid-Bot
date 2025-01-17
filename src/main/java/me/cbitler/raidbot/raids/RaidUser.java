package me.cbitler.raidbot.raids;

/**
 * Represents a raid user
 * This class is not commented as the method names should be self-explanatory
 * @author Christopher Bitler
 */
public class RaidUser {
    String id;
    String name;
    String spec;
    String role;
    String discriminator;

    public RaidUser(String id, String name, String spec, String role, String discriminator) {
        this.id = id;
        this.name = name;
        this.spec = spec;
        this.role = role;
        this.discriminator = discriminator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }
    
    
}

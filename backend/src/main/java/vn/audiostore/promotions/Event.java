package vn.audiostore.promotions;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="events")
public class Event {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=160) private String name;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Column(nullable=false) private boolean active=true;
    protected Event(){}
    public Event(String name,Instant startsAt,Instant endsAt){this.name=name;this.startsAt=startsAt;this.endsAt=endsAt;}
    public Long getId(){return id;} public String getName(){return name;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public boolean isActive(){return active;}
    public void update(String name,Instant startsAt,Instant endsAt,boolean active){this.name=name;this.startsAt=startsAt;this.endsAt=endsAt;this.active=active;}
}

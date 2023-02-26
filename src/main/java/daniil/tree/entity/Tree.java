package daniil.tree.entity;

import javax.persistence.*;

@Entity
@Table(name = "tree")
public class Tree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int right_key;
    private int left_key;
    private short level;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRight_key() {
        return right_key;
    }

    public void setRight_key(int right_key) {
        this.right_key = right_key;
    }

    public int getLeft_key() {
        return left_key;
    }

    public void setLeft_key(int left_key) {
        this.left_key = left_key;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


package daniil.tree;

import daniil.tree.entity.Tree;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class Application {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();

        TypedQuery<Tree> treeTypedQuery = manager.createQuery("select t from Tree t order by t.left_key", Tree.class);
            List<Tree> treeList = treeTypedQuery.getResultList();

            for (Tree tree : treeList){
                int level = tree.getLevel();
                System.out.println(("- ".repeat(level) + tree.getName()).trim());
            }
    }
}

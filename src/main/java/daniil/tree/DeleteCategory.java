package daniil.tree;

import daniil.tree.entity.Tree;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DeleteCategory {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        Long id = 0L;

        try {
            System.out.print("Введите ID удаляемой позиции: ");
            id = Long.parseLong(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        TypedQuery<Tree> getCategoryListQuery = manager.createQuery(
                "select t from Tree t order by t.left_key", Tree.class);

        Tree category = manager.find(Tree.class, id);
        int rightKey = category.getRight_key();
        int leftKey = category.getLeft_key();

        int n = 0;
        try {
            manager.getTransaction().begin();
            List<Tree> categories = getCategoryListQuery.getResultList();
            for(Tree cat : categories){
                if(cat.getLeft_key() >= leftKey && cat.getRight_key() <= rightKey){
                    manager.remove(cat);
                    n++;
                }
            }

            List<Tree> categories2 = getCategoryListQuery.getResultList();
            // Уменьшение значений left_key и right_key
            for (Tree cat : categories2){
                if(cat.getLeft_key() >= (rightKey)){
                    cat.setLeft_key(cat.getLeft_key()-2*n);
                }
                if (cat.getRight_key() >= rightKey){
                    cat.setRight_key(cat.getRight_key()-2*n);
                }
                manager.persist(cat);
            }
            manager.getTransaction().commit();
            System.out.println("Данные удалены успешно!");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }

    }
}

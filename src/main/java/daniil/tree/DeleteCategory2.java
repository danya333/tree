package daniil.tree;

import daniil.tree.entity.Tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeleteCategory2 {
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

        Tree category = manager.find(Tree.class, id);
        int rightKey = category.getRight_key();
        int leftKey = category.getLeft_key();

        int count = rightKey - leftKey + 1;

        try {
            manager.getTransaction().begin();
            Query deleteQuery = manager.createQuery(
                    "delete from Tree t where t.left_key >= ?1 and t.right_key <= ?2");
            deleteQuery.setParameter(1, leftKey);
            deleteQuery.setParameter(2, rightKey);
            deleteQuery.executeUpdate();


            Query updateQuery = manager.createQuery(
                    "update Tree t set t.left_key = t.left_key - ?1 where t.left_key > ?2");
            updateQuery.setParameter(1, count);
            updateQuery.setParameter(2, rightKey);
            updateQuery.executeUpdate();

            Query updateQuery2 = manager.createQuery(
                    "update Tree t set t.right_key = t.right_key - ?1 where t.right_key > ?2");
            updateQuery2.setParameter(1, count);
            updateQuery2.setParameter(2, rightKey);
            updateQuery2.executeUpdate();

            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
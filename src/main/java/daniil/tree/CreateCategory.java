package daniil.tree;

import daniil.tree.entity.Tree;
import jdk.jfr.Category;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CreateCategory {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String id = "";
        String name = "";

        try {
            manager.getTransaction().begin();
            System.out.print("Введите ID категории: ");
            id = in.readLine();
            System.out.print("Введите название подкатегории: ");
            name = in.readLine();

            if (id.isEmpty()) {
                TypedQuery<Integer> getMaxRightKeyQuery = manager.createQuery(
                        "select max(t.right_key) from Tree t", Integer.class);
                int maxRightKey = getMaxRightKeyQuery.getSingleResult();
                Tree tree = new Tree();
                tree.setName(name);
                tree.setLeft_key(maxRightKey + 1);
                tree.setRight_key(maxRightKey + 2);
                tree.setLevel((short) 0);
                manager.persist(tree);
            } else {
                Tree categoryIn = manager.find(Tree.class, Long.parseLong(id));
                short categoryInLevel = categoryIn.getLevel();
                int rightKey = categoryIn.getRight_key();

                // Увеличение значений left_key и right_key
                Query updateLeftKeysQuery = manager.createQuery("update Tree t set t.left_key = t.left_key + 2 " +
                        "where t.left_key > ?1");
                updateLeftKeysQuery.setParameter(1, rightKey);
                updateLeftKeysQuery.executeUpdate();

                Query updateRightKeysQuery = manager.createQuery("update Tree t set t.right_key = t.right_key + 2 " +
                        "where t.right_key >= ?1");
                updateRightKeysQuery.setParameter(1, rightKey);
                updateRightKeysQuery.executeUpdate();

                // Создание нового объекта
                Tree tree = new Tree();
                tree.setName(name);
                tree.setLeft_key(rightKey);
                tree.setRight_key(rightKey + 1);
                tree.setLevel((short) (categoryInLevel + 1));
                manager.persist(tree);
            }
            manager.getTransaction().commit();
            System.out.println("Данные добавлены успешно!");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}

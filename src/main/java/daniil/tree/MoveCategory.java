package daniil.tree;

import daniil.tree.entity.Tree;
import org.hibernate.mapping.Collection;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class MoveCategory {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        Long id = 0L;
        Long idTo = 0L;

        try {
            System.out.print("Введите ID перемещаемой позиции: ");
            id = Long.parseLong(in.readLine());
            System.out.print("Введите ID новой категории: ");
            idTo = Long.parseLong(in.readLine());

            Tree movedCategory = manager.find(Tree.class, id);
            int movedRightKey = movedCategory.getRight_key();
            int movedLeftKey = movedCategory.getLeft_key();

            manager.getTransaction().begin();

            // 1) Сделать ключи перемещаемой категории отрицательными.
            Query minusQuery = manager.createQuery(
                    "update Tree t set t.left_key = 0 - t.left_key, t.right_key = 0 - t.right_key" +
                            " where t.left_key >= ?1 and t.right_key <= ?2");
            minusQuery.setParameter(1, movedLeftKey);
            minusQuery.setParameter(2, movedRightKey);
            minusQuery.executeUpdate();

            // 2) Убрать образовавшийся промежуток.
            int count = movedRightKey - movedLeftKey + 1;
            Query updateQuery = manager.createQuery(
                    "update Tree t set t.left_key = t.left_key - ?1 where t.left_key > ?2");
            updateQuery.setParameter(1, count);
            updateQuery.setParameter(2, movedRightKey);
            updateQuery.executeUpdate();

            Query updateQuery2 = manager.createQuery(
                    "update Tree t set t.right_key = t.right_key - ?1 where t.right_key > ?2");
            updateQuery2.setParameter(1, count);
            updateQuery2.setParameter(2, movedRightKey);
            updateQuery2.executeUpdate();

            Tree categoryTo = manager.find(Tree.class, idTo);
            int toRightKey = categoryTo.getRight_key() - movedLeftKey;

            if(idTo == 0){
                // 4) Пересчитать отрицательные ключи в положительные в 0 категории.
                TypedQuery<Integer> getMaxRightKeyQuery = manager.createQuery(
                        "select max(t.right_key) from Tree t", Integer.class);
                int maxRightKey = getMaxRightKeyQuery.getSingleResult();

                Query updateQuery6 = manager.createQuery(
                        "update Tree t set t.left_key = 0 - t.left_key - ?3 + ?1 + 1, " +
                                "t.right_key = 0 - t.right_key - ?4 + ?1 + 1, " +
                                "t.level = t.level - ?2 + 1" +
                                "where t.left_key < 0");
                updateQuery6.setParameter(1, maxRightKey);
                updateQuery6.setParameter(2, movedCategory.getLevel());
                updateQuery6.setParameter(3, movedLeftKey);
                updateQuery6.setParameter(4, movedRightKey);
                updateQuery6.executeUpdate();
            } else{
                // 3) Выделить место в новой родительской категории.
                Query updateQuery3 = manager.createQuery(
                        "update Tree t set t.left_key = t.left_key + ?1 where t.left_key > ?2");
                updateQuery3.setParameter(1, count);
                updateQuery3.setParameter(2, categoryTo.getRight_key());
                updateQuery3.executeUpdate();

                Query updateQuery4 = manager.createQuery(
                        "update Tree t set t.right_key = t.right_key + ?1 where t.right_key >= ?2");
                updateQuery4.setParameter(1, count);
                updateQuery4.setParameter(2, categoryTo.getRight_key());
                updateQuery4.executeUpdate();

                // 4) Пересчитать отрицательные ключи в положительные в новую родительскую категорию.
                Query updateQuery6 = manager.createQuery(
                        "update Tree t set t.right_key = 0 - t.right_key + ?1, t.left_key = 0 - t.left_key + ?1, t.level = t.level - ?2 + ?3 + 1" +
                                "where t.left_key < 0");
                updateQuery6.setParameter(1, toRightKey);
                updateQuery6.setParameter(2, movedCategory.getLevel());
                updateQuery6.setParameter(3, categoryTo.getLevel());
                updateQuery6.executeUpdate();
            }
            manager.getTransaction().commit();

        } catch (IOException e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }


    }
}
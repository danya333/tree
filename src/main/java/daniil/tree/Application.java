package daniil.tree;

import daniil.tree.entity.Tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Application {
    public static void main(String[] args) {

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        TypedQuery<Tree> treeTypedQuery = manager.createQuery("select t from Tree t order by t.left_key", Tree.class);
        List<Tree> treeList = treeTypedQuery.getResultList();

        for (Tree tree : treeList) {
            int level = tree.getLevel();
            System.out.println(("- ".repeat(level) + tree.getName()).trim());
        }

        System.out.println();
        System.out.println("Выберите действие:");
        System.out.println(" - Добавить [1]");
        System.out.println(" - Переместить [2]");
        System.out.println(" - Удалить [3]");

        try {
            int answer = Integer.parseInt(in.readLine());
            if (answer == 1) {
                create(manager, in);
            } else if (answer == 2) {
                move(manager, in);
                System.out.println("Товар перемещён успешно!");
            } else if (answer == 3) {
                delete(manager, in);
                System.out.println("Товар удалён успешно!");
            } else {
                System.out.println("Некорректный ответ!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void create(EntityManager manager, BufferedReader in) {
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

    public static void move(EntityManager manager, BufferedReader in) {

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

            manager.getTransaction().commit();

        } catch (IOException e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public static void delete(EntityManager manager, BufferedReader in) {

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

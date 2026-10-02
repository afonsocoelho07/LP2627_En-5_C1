package upt.pt;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) {
        SessionFactory sf = new Configuration().configure().buildSessionFactory();

        try (Session session = sf.openSession()) {
            session.beginTransaction();
            session.persist(new Item("Mochila azul encontrada na biblioteca"));
            session.getTransaction().commit();
            System.out.println("Item guardado com sucesso!");
        }

        sf.close();
    }
}
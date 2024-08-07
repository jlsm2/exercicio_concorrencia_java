import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

class Banheiro {
    private final int capacidade;
    private int pessoasDentro;
    private String sexoBanheiro;
    private final Semaphore semaphore;
    private final Lock lock;

    public Banheiro(int capacidade) {
        this.capacidade = capacidade;
        this.pessoasDentro = 0;
        this.sexoBanheiro = null;
        this.semaphore = new Semaphore(capacidade, true);
        this.lock = new ReentrantLock(true);
    }

    public void entrar(String sexoPessoa, int id) throws InterruptedException {
        while (true) {
            semaphore.acquire();
            lock.lock();
            Thread.sleep(1000);
            try {
                if (sexoBanheiro == null || sexoBanheiro.equals(sexoPessoa)) {
                    pessoasDentro++;
                    sexoBanheiro = sexoPessoa;
                    System.out.println("Pessoa " + id + " (" + sexoPessoa + ")" + " entrou no banheiro [" + pessoasDentro + "/" + capacidade + "]");
                    break;
                } else {
                    semaphore.release();
                    System.out.println("Pessoa " + id + " (" + sexoPessoa + ")" + " não pode entrar no banheiro pois já há " + sexoBanheiro + " dentro.");
                }
            } finally {
                lock.unlock();
            }
            Thread.sleep(500); // tempo de espera para tentar entrar novamente
        }
    }

    public void sair(String sexoPessoa, int id) throws InterruptedException {
        lock.lock();
        Thread.sleep(1000);
        try {
            pessoasDentro--;
            System.out.println("Pessoa " + id + " (" + sexoPessoa + ")" + " saiu do banheiro [" + pessoasDentro + "/" + capacidade + "]");
            if (pessoasDentro == 0) {
                sexoBanheiro = null;
            }
            semaphore.release();
        } finally {
            lock.unlock();
        }
    }
}

class Pessoa implements Runnable {
    private final Banheiro banheiro;
    private final String sexoPessoa;
    private final int id;

    public Pessoa(Banheiro banheiro, String sexoPessoa, int id) {
        this.banheiro = banheiro;
        this.sexoPessoa = sexoPessoa;
        this.id = id;
    }

    public void run() {
        try {
            banheiro.entrar(sexoPessoa, id);
            Thread.sleep((long) (Math.random() * 1000));
            banheiro.sair(sexoPessoa, id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro(3);

        for (int i = 1; i <= 100; i++) {
            Pessoa pessoa = new Pessoa(banheiro, i % 2 == 0 ? "homem" : "mulher", i);
            new Thread(pessoa).start();
        }
    }
}

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
        lock.lock();
        try {
            if (sexoBanheiro != null && !sexoPessoa.equals(sexoBanheiro)) {
                System.out.println("Pessoa " + id + " (" + sexoPessoa + ")" + " não pode entrar no banheiro pois já há " + sexoBanheiro + " dentro.");
            }
            while (pessoasDentro == capacidade || (sexoBanheiro != null && !sexoBanheiro.equals(sexoPessoa))) {
                lock.unlock();
                Thread.sleep(50);
                lock.lock();
            }
            semaphore.acquire();
            pessoasDentro++;
            sexoBanheiro = sexoPessoa;
            System.out.println("Pessoa " + id + " (" + sexoPessoa + ")" + " entrou no banheiro [" + pessoasDentro + "/" + capacidade + "]");
        } finally {
            lock.unlock();
        }
    }
    
    public void sair(String sexoPessoa, int id) {
        lock.lock();
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
    private int id;

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

        for (int i = 1; i <= 10; i++) {
            Pessoa pessoa = new Pessoa(banheiro, i % 2 == 0 ? "homem" : "mulher", i);
            new Thread(pessoa).start();
        }
    }
}

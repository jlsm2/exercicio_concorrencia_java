import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class Barbearia {
    private final int numCadeiras;
    private Semaphore cadeirasLivres;
    private Semaphore dormindo;
    private Semaphore cortando;
    private ReentrantLock lock;
    public Barbearia(int numCadeiras) {
        this.numCadeiras = numCadeiras;
        this.cadeirasLivres = new Semaphore(numCadeiras);
        this.cortando = new Semaphore(0);
        this.dormindo = new Semaphore(0);
        this.lock = new ReentrantLock();
    }

    public void cortar(int cliente) throws InterruptedException {
        lock.lock();
        System.out.println("O cliente " + cliente + " acabou de entrar na Barbearia!");

        if (cadeirasLivres.availablePermits() > 0) {
            cadeirasLivres.acquire();
            lock.unlock();

            cortando.acquire();
            dormindo.release();

            System.out.println(cliente + " está cortando o cabelo agora.");

            Thread.sleep(2000); // tempo para cortar o cabelo

            System.out.println("Corte finalizado, " + cliente + " adorou o corte e está indo embora.");

            cadeirasLivres.release();
        } else {
            lock.unlock();
            System.out.println("Parece que a Barbearia está cheia e o cliente irá embora");
        }
    }

    public void barbear() {
        while (true) {
            try {
                dormindo.acquire();
                cortando.release();
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Cliente extends Thread {
    private int cliente;
    private Barbearia barbearia;

    public Cliente(int cliente, Barbearia barbearia) {
        this.cliente = cliente;
        this.barbearia = barbearia;
    }

    @Override
    public void run() {
        try {
            barbearia.cortar(cliente);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Barbeiro extends Thread {
    private Barbearia barbearia;

    public Barbeiro(Barbearia barbearia) {
        this.barbearia = barbearia;
    }

    @Override
    public void run() {
        barbearia.barbear();
    }
}

public class Main {
    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(5);

        Barbeiro barbeiro = new Barbeiro(barbearia);
        barbeiro.start();

        for (int i = 1; i <= 10; i++) {
            Cliente cliente = new Cliente(i, barbearia);
            cliente.start();

            try {
                Thread.sleep(1000); // intervalo de chegada dos clientes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

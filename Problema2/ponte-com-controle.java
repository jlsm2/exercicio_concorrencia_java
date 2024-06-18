import java.util.concurrent.Semaphore;

class Carro implements Runnable {
    private String direcao;
    private int id;
    private Semaphore semaforo;

    Carro(String direcao, int id, Semaphore semaforo) {
        this.direcao = direcao;
        this.id = id;
        this.semaforo = semaforo;
    }

    public void run() {
        try {
            semaforo.acquire();
            atravessar();
            semaforo.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void atravessar() throws InterruptedException {
        System.out.println("Carro " + id + " vindo da " + direcao + " entrou na ponte.");
        Thread.sleep(1000);
        System.out.println("Carro " + id + " vindo da " + direcao + " saiu da ponte.");
    }
}

public class Threads {
    public static void main(String[] args) {
        Semaphore semaforo = new Semaphore(1);

        for (int i = 1; i <= 10; i++) {
            String direcao = (i % 2 == 0) ? "esquerda" : "direita";
            new Thread(new Carro(direcao, i, semaforo)).start();
        }
    }
}

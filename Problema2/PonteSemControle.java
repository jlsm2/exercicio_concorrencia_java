class Carro implements Runnable {
    private String direcao;
    private int id;

    Carro(String direcao, int id) {
        this.direcao = direcao;
        this.id = id;
    }

    public void run() {
        try {
            System.out.println("Carro " + id + " vindo da " + direcao + " entrou na ponte.");
            Thread.sleep(1000);
            System.out.println("Carro " + id + " vindo da " + direcao + " saiu da ponte.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class PonteSemControle {
    public static void main(String[] args) {
        for (int i = 1; i <= 5; i++) {
            String direcao = (i % 2 == 0) ? "esquerda" : "direita";
            new Thread(new Carro(direcao, i)).start();
        }
    }
} 

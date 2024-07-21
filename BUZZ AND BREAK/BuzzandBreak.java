import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class BuzzandBreak extends JPanel implements ActionListener {

    private Timer timer;
    private Mosquito mosquito;
    private Paddle paddle;
    private HumanBrick[][] bricks;
    private int rowCount = 5;
    private int colCount = 8;
    private int totalBricks = rowCount * colCount;
    private int score = 0;
    private BufferedImage bgImage;
    
    public BuzzandBreak() {
        setFocusable(true);
        setPreferredSize(new Dimension(850, 650));

        try{
            bgImage = ImageIO.read(new File("background.jpg"));
        }catch(IOException e){
            e.printStackTrace();
        }

        mosquito = new Mosquito();
        paddle = new Paddle();
        bricks = new HumanBrick[rowCount][colCount];

        int brickWidth = 90;
        int brickHeight = 55;
        int spacing = 4;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                String imagePath = "man" +((i* colCount+j)%5+1)+".png";
                bricks[i][j] = new HumanBrick (j * (brickWidth + spacing) + 50, i * (brickHeight + spacing) + 50, brickWidth, brickHeight, imagePath);
            }
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                paddle.keyPressed(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                paddle.mouseMoved(e);
            }
        });

        timer = new Timer(10, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mosquito.move();
        paddle.move();
        checkCollision();
        repaint();
    }

    private void checkCollision() {
        if (mosquito.getRect().intersects(paddle.getRect())) {
            mosquito.setYDir(-mosquito.getYDir());
        }

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                HumanBrick b = bricks[i][j];
                if (!b.isDestroyed() && b.getShape().intersects(mosquito.getRect())) {
                    mosquito.setYDir(-mosquito.getYDir());
                    b.setDestroyed(true);
                    score += 10;
                    totalBricks--;
                    if (totalBricks == 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(this, "You won!", "Wohooo :)", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }

        if (mosquito.getY() > getHeight()) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over.\n Final Score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // BG image
        if(bgImage != null){
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        }

        mosquito.draw(g);
        paddle.draw(g);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (!bricks[i][j].isDestroyed()) {
                    bricks[i][j].draw(g);
                }
            }
        }

    //Score
    g.setColor(Color.WHITE);
    g.setFont(new Font("Lato", Font.BOLD, 20));
    g.drawString("Score:"+score, 10, 30);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Buzz And Break");
        BuzzandBreak game = new BuzzandBreak();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Mosquito {
    private int x = 400;
    private int y = 300;
    private int xDir = -1;
    private int yDir = -2;
    private BufferedImage image;
    private final int SIZE = 28;

    public Mosquito(){
        try{
            image = ImageIO.read(new File("mosq.png"));
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void move() {
        x += xDir;
        y += yDir;

        if (x <= 0 || x >= 800 - SIZE) {
            xDir = -xDir;
        }
        if (y <= 0) {
            yDir = -yDir;
        }
    }

    public void setYDir(int yDir) {
        this.yDir = yDir;
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y,SIZE,SIZE,null);
    }

    public int getY() {
        return y;
    }

    public int getYDir() {
        return yDir;
    }
}

class Paddle {
    private int x = 350;
    private final int Y = 550;
    private final int WIDTH = 80;
    private final int HEIGHT = 42;
    private int moveX = 0;
    private BufferedImage image;

    public Paddle(){
        try{
            image = ImageIO.read(new File("paddle.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void move() {
        if (x + moveX >= 0 && x + moveX <= 800 - WIDTH) {
            x += moveX;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            moveX = -5;
        }

        if (key == KeyEvent.VK_RIGHT) {
            moveX = 5;
        }
    }

    public void mouseMoved(MouseEvent e) {
        x = e.getX() - WIDTH / 2;
    }

    public Rectangle getRect() {
        return new Rectangle(x, Y, WIDTH, HEIGHT);
    }

    public void draw(Graphics g) {
        if(image != null){
            g.drawImage(image , x,Y, WIDTH,HEIGHT, null);
        }else{
            g.setColor(Color.yellow);
            g.fillRect(x,Y,WIDTH, HEIGHT);
            g.setColor(Color.black);
            g.drawRect(x,Y,WIDTH, HEIGHT);
        

        }
    }
}

class HumanBrick {
    private int x;
    private int y;
    private final int WIDTH;
    private final int HEIGHT;
    private boolean destroyed = false;
    private BufferedImage image;

    public HumanBrick(int x, int y, int width, int height, String imagePath) {
        this.x = x;
        this.y = y;
        this.WIDTH = width;
        this.HEIGHT = height;

        try{
            this.image = ImageIO.read(new File("man.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Rectangle getRect(){
        return new Rectangle(x, y, WIDTH,HEIGHT);
    }

    

    public Shape getShape() {
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(x, y + HEIGHT);
        triangle.lineTo(x + WIDTH / 2, y);
        triangle.lineTo(x + WIDTH, y + HEIGHT);
        triangle.closePath();
        return triangle;
    }

    public void draw(Graphics g) {
        if(!destroyed){
            g.drawImage(image,x,y,WIDTH,HEIGHT, null);
        }
        
    }
}

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

interface Attackable {
    int attack();
}

class Item implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private final String id; // immutable
    private String name;
    private int quantity;

    public Item(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }

    @Override
    public String toString() {
        return String.format("Item[%s] %s x%d", id, name, quantity);
    }

    @Override
    public Item clone() throws CloneNotSupportedException {
        try { return (Item) super.clone(); } catch (CloneNotSupportedException e) { return null; }
    }
}

// Generic itemset to show use of generics
class ItemSet<T extends Item> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<T> items = new ArrayList<>();

    public void add(T item) { items.add(item); }
    public boolean removeById(String id) {
        return items.removeIf(i -> i.getId().equals(id));
    }
    public T findById(String id) {
        for (T i : items)
            if (i.getId().equals(id)) return i;
        return null;
    }
    public List<T> list() { return Collections.unmodifiableList(items); }
    @Override
    public String toString() { return items.toString(); }
}

class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ItemSet<Item> itemSet = new ItemSet<>(); // composition

    public void addItem(Item it) { itemSet.add(it); }
    public boolean removeItem(String id) { return itemSet.removeById(id); }
    public Item find(String id) { return itemSet.findById(id); }
    public List<Item> getItems() { return itemSet.list(); }
}

// Weapon Hierarchy (Inheritance)
abstract class Weapon implements Serializable, Attackable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected int baseDamage;

    public Weapon(String name, int baseDamage) {
        this.name = name; this.baseDamage = baseDamage;
    }

    public String getName() { return name; }
    public int getBaseDamage() { return baseDamage; }

    @Override
    public abstract int attack();

    @Override
    public String toString() {
        return String.format("%s ( dmg=%d)", name, baseDamage);
    }
}

class Sword extends Weapon {
    public Sword(String name, int baseDamage) { super(name, baseDamage); }
    @Override
    public int attack() { return baseDamage + new Random().nextInt(6); }
}
class Bow extends Weapon {
    public Bow(String name, int baseDamage) { super(name, baseDamage); }
    @Override
    public int attack() { return baseDamage + new Random().nextInt(8); }
}
class Staff extends Weapon {
    public Staff(String name, int baseDamage) { super(name, baseDamage); }
    @Override
    public int attack() { return baseDamage + new Random().nextInt(10); }
}

// Skill (Aggregation)
class Skill implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private static int skillCounter = 0; // static counter
    private final int skillId;
    private String name;
    private int power;

    public Skill(String name, int power) {
        this.skillId = ++skillCounter;
        this.name = name; this.power = power;
    }

    public int getSkillId() { return skillId; }
    public String getName() { return name; }
    public int getPower() { return power; }

    @Override
    public String toString() { return String.format("Skill#%d %s(p=%d)", skillId, name, power); }

    @Override
    public Skill clone() throws CloneNotSupportedException {
        try { return (Skill) super.clone(); } catch (CloneNotSupportedException e) { return null; }
    }
}

//Abstract Character (Comparable & Cloneable) 
abstract class Character implements Serializable, Comparable<Character>, Cloneable {
    private static final long serialVersionUID = 1L;

    private final String characterId; // immutable
    private String name;
    private int level;
    private Integer xp; // wrapper class
    private Inventory inventory; // composition
    private ArrayList<Skill> skills; // aggregation
    protected Weapon weapon; // association

    // constructor overloading
    public Character(String characterId, String name) {
        this(characterId, name, 1, 0, null);
    }
    @SuppressWarnings("UnnecessaryBoxing")
    public Character(String characterId, String name, int level, int xp, Weapon w) {
        this.characterId = characterId;
        this.name = name; this.level = level; this.xp = Integer.valueOf(xp);
        this.inventory = new Inventory();
        this.skills = new ArrayList<>();
        this.weapon = w;
    }

    // Encapsulation: getters + setters with validation
    public String getCharacterId() { return characterId; }
    public String getName() { return name; }
    public void setName(String name) { if (name != null && !name.isBlank()) this.name = name; }
    public int getLevel() { return level; }
    public Integer getXp() { return xp; }
    public void addXp(int amount) {
        if (amount <= 0) return;
        this.xp += amount;
        while (this.xp >= level * 100) { this.xp -= level * 100; level++; }
    }

    public Inventory getInventory() { return inventory; }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }

    // Aggregation: Character "has" skills (not owning lifecycle)
    public void addSkill(Skill s) { if (s != null) skills.add(s); }
    // Overloaded addSkill by details
    public void addSkill(String name, int power) { addSkill(new Skill(name, power)); }

    public void equipWeapon(Weapon w) { this.weapon = w; }

    public abstract int attack(); // polymorphic

    @Override
    public String toString() {
        return String.format("%s[id=%s level=%d xp=%d weapon=%s skills=%d]",
                name, characterId, level, xp, weapon==null?"none":weapon.getName(), skills.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Character)) return false;
        Character c = (Character) o;
        return this.characterId.equals(c.characterId) && this.name.equals(c.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(characterId, name);
    }

    @Override
    public int compareTo(Character o) {
        // compare by level then XP
        if (this.level != o.level) return Integer.compare(this.level, o.level);
        return Integer.compare(this.xp, o.xp);
    }

    @Override
    public Character clone() throws CloneNotSupportedException {
        try {
            Character copy = (Character) super.clone();
            copy.inventory = new Inventory(); // shallow empty inventory for safety
            copy.skills = new ArrayList<>();
            for (Skill s : this.skills) copy.skills.add(s.clone());
            return copy;
        } catch (CloneNotSupportedException e) { return null; }
    }
}

// Subclasses
class Warrior extends Character {
    private static final long serialVersionUID = 1L;
    private int rage;

    public Warrior(String id, String name) {
        super(id, name);
        this.rage = 0;
    }
    public Warrior(String id, String name, Weapon w) {
        this(id, name);
        this.weapon = w;
    }

    @Override
    public int attack() {
        int base = (weapon!=null?weapon.attack():5) + getLevel()*2;
        int bonus = (rage>0)? (rage * 2) : 0;
        rage = Math.max(0, rage-1);
        return base + bonus;
    }
    public void scream() { rage += 2; }
}

class Mage extends Character {
    private static final long serialVersionUID = 1L;
    private int mana;
    public Mage(String id, String name) { super(id, name); this.mana=100; }
    public Mage(String id, String name, Weapon w) {
        this(id, name);
        this.weapon = w;
    }

    @Override
    public int attack() {
        if (mana >= 20) { mana -= 20; return (weapon!=null?weapon.attack():3) + 10 + getLevel(); }
        return (weapon!=null?weapon.attack():3) + getLevel();
    }
    public void meditate() { mana = Math.min(100, mana+30); }
}

class Archer extends Character {
    private static final long serialVersionUID = 1L;
    private int arrows;
    public Archer(String id, String name) { super(id, name); this.arrows=20; }
    public Archer(String id, String name, Weapon w) {
        this(id, name);
        this.weapon = w;
    }

    @Override
    public int attack() {
        if (arrows>0) { arrows--; return (weapon!=null?weapon.attack():4) + 5 + getLevel(); }
        return 1 + getLevel();
    }
    public void refill(int n) { arrows += n; }
}

// ---------- Game Manager: handles saving/loading, battles, menus ----------
class GameManager {
    private final ArrayList<Character> roster = new ArrayList<>();

    public void players() {
        Warrior w = new Warrior("W-001", "Thorin", new Sword("Iron Blade", 12));
        w.addSkill("Slash", 8);
        Mage m = new Mage("M-099", "Elowen", new Staff("Oak Staff", 10));
        m.addSkill("Fireball", 15);
        Archer a = new Archer("A-555", "Lyria", new Bow("Shortbow", 9));
        a.addSkill("Piercing Shot", 12);
        roster.add(w); roster.add(m); roster.add(a);
    }

    public ArrayList<Character> getRoster() {
        return roster;
    }

    public void saveRoster(String fn) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fn))) {
            oos.writeObject(roster);
        }
    }

    public void loadRoster(String fn) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fn))) {
            Object o = ois.readObject();
            if (o instanceof ArrayList) {
                ArrayList<?> arr = (ArrayList<?>) o;
                roster.clear();
                for (Object x : arr) {
                    if (x instanceof Character character) {
                        roster.add(character);
                    }
                }
            } else {
                throw new IOException("File content invalid.");
            }
        }
    }
}

//  Main Program 
public class CursedKingdom extends JFrame {
    private final GameManager gm = new GameManager();
    private final CardLayout cardLayout = new CardLayout();
    @SuppressWarnings("FieldMayBeFinal")
    private JPanel mainPanel = new JPanel(cardLayout);
    private final Font bloodyFont = new Font("Chiller", Font.BOLD, 25);
    private final DefaultListModel<Character> charModel = new DefaultListModel<>();
    private Character currentViewedChar;
    private JTextArea viewDetailsArea;
    private Character battleHero;
    private Character battleEnemy;
    private int hpHero;
    private int hpEnemy;
    private JLabel heroHpLabel;
    private JLabel enemyHpLabel;
    private JTextArea battleLogArea;
    private final Random rnd = new Random();
    private final String[] scaryLines = {
        "The shadows whisper your doom...",
        "Blood shall flow in the cursed kingdom...",
        "Heroes fall, the curse prevails...",
        "Enter the realm where souls scream eternally...",
        "The kingdom's curse hungers for your soul..."
    };

    public CursedKingdom() {
        setTitle("The Cursed Kingdom");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);

        gm.players();
        updateCharModel();

        JLabel titleLabel = new JLabel("Welcome to The Cursed Kingdom!");
        titleLabel.setForeground(Color.RED);
        titleLabel.setFont(bloodyFont.deriveFont(32f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        createMainMenuPanel();
        createListCharactersPanel();
        createCreateCharacterPanel();
        createViewCharacterPanel();
        createBattlePanel();

        cardLayout.show(mainPanel, "MainMenu");
        setVisible(true);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.RED);
        btn.setFont(bloodyFont);
    }

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.RED);
        lbl.setFont(bloodyFont);
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(Color.DARK_GRAY);
        tf.setForeground(Color.WHITE);
        tf.setFont(bloodyFont);
    }

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(Color.BLACK);
        ta.setForeground(Color.WHITE);
        ta.setFont(bloodyFont);
        ta.setEditable(false);
    }

    private void styleList(JList<?> list) {
        list.setBackground(Color.BLACK);
        list.setForeground(Color.WHITE);
        list.setFont(bloodyFont);
    }

    private void updateCharModel() {
        charModel.clear();
        for (Character c : gm.getRoster()) {
            charModel.addElement(c);
        }
    }

    private void createMainMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 0, 10));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel scaryLabel = new JLabel(scaryLines[rnd.nextInt(scaryLines.length)]);
        styleLabel(scaryLabel);
        scaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(scaryLabel);

        JButton listBtn = new JButton("List Characters");
        styleButton(listBtn);
        listBtn.addActionListener(e -> cardLayout.show(mainPanel, "ListCharacters"));
        panel.add(listBtn);

        JButton createBtn = new JButton("Create Character");
        styleButton(createBtn);
        createBtn.addActionListener(e -> cardLayout.show(mainPanel, "CreateCharacter"));
        panel.add(createBtn);

        JButton viewBtn = new JButton("View Character Details");
        styleButton(viewBtn);
        viewBtn.addActionListener(e -> cardLayout.show(mainPanel, "ListCharacters")); // Go to list to select
        panel.add(viewBtn);

        JButton battleBtn = new JButton("Battle Simulator (vs AI)");
        styleButton(battleBtn);
        battleBtn.addActionListener(e -> cardLayout.show(mainPanel, "ListCharacters")); // Go to list to select for battle
        panel.add(battleBtn);

        JButton saveBtn = new JButton("Save Game");
        styleButton(saveBtn);
        saveBtn.addActionListener(e -> handleSave());
        panel.add(saveBtn);

        JButton loadBtn = new JButton("Load Game");
        styleButton(loadBtn);
        loadBtn.addActionListener(e -> handleLoad());
        panel.add(loadBtn);

        JButton exitBtn = new JButton("Exit");
        styleButton(exitBtn);
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn);

        mainPanel.add(panel, "MainMenu");
    }

    private void createListCharactersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Characters:");
        styleLabel(label);
        panel.add(label, BorderLayout.NORTH);

        JList<Character> charList = new JList<>(charModel);
        styleList(charList);
        JScrollPane scroll = new JScrollPane(charList);
        scroll.getViewport().setBackground(Color.BLACK);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton viewBtn = new JButton("View Selected");
        styleButton(viewBtn);
        viewBtn.addActionListener(e -> {
            Character selected = charList.getSelectedValue();
            if (selected != null) {
                currentViewedChar = selected;
                updateViewDetails();
                cardLayout.show(mainPanel, "ViewCharacter");
            }
        });
        buttonPanel.add(viewBtn);

        JButton battleBtn = new JButton("Battle with Selected");
        styleButton(battleBtn);
        battleBtn.addActionListener(e -> {
            Character selected = charList.getSelectedValue();
            if (selected != null) {
                startBattle(selected);
            }
        });
        buttonPanel.add(battleBtn);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        buttonPanel.add(backBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "ListCharacters");
    }

    @SuppressWarnings("ConvertToStringSwitch")
    private void createCreateCharacterPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel idLabel = new JLabel("ID:");
        styleLabel(idLabel);
        JTextField idField = new JTextField();
        styleTextField(idField);
        panel.add(idLabel);
        panel.add(idField);

        JLabel nameLabel = new JLabel("Name:");
        styleLabel(nameLabel);
        JTextField nameField = new JTextField();
        styleTextField(nameField);
        panel.add(nameLabel);
        panel.add(nameField);

        JLabel typeLabel = new JLabel("Type:");
        styleLabel(typeLabel);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Warrior", "Mage", "Archer"});
        typeBox.setBackground(Color.BLACK);
        typeBox.setForeground(Color.RED);
        typeBox.setFont(bloodyFont);
        panel.add(typeLabel);
        panel.add(typeBox);

        JButton createBtn = new JButton("Create");
        styleButton(createBtn);
        createBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            Character c;
            if ("Warrior".equals(type)) {
                c = new Warrior(id, name, new Sword("Starter Sword", 7));
            } else if ("Mage".equals(type)) {
                c = new Mage(id, name, new Staff("Starter Staff", 6));
            } else if ("Archer".equals(type)) {
                c = new Archer(id, name, new Bow("Starter Bow", 6));
            } else {
                c = new Warrior(id, name, new Sword("Starter Sword", 7));
            }
            gm.getRoster().add(c);
            updateCharModel();
            idField.setText("");
            nameField.setText("");
            cardLayout.show(mainPanel, "MainMenu");
        });
        panel.add(createBtn);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        panel.add(backBtn);

        mainPanel.add(panel, "CreateCharacter");
    }

    private void createViewCharacterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        viewDetailsArea = new JTextArea();
        styleTextArea(viewDetailsArea);
        JScrollPane scroll = new JScrollPane(viewDetailsArea);
        scroll.getViewport().setBackground(Color.BLACK);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton addSkillBtn = new JButton("Add Skill");
        styleButton(addSkillBtn);
        addSkillBtn.addActionListener(e -> addSkillToCurrent());
        buttonPanel.add(addSkillBtn);

        JButton equipWeaponBtn = new JButton("Equip Weapon");
        styleButton(equipWeaponBtn);
        equipWeaponBtn.addActionListener(e -> equipWeaponToCurrent());
        buttonPanel.add(equipWeaponBtn);

        JButton addItemBtn = new JButton("Add Item");
        styleButton(addItemBtn);
        addItemBtn.addActionListener(e -> addItemToCurrent());
        buttonPanel.add(addItemBtn);

        JButton removeItemBtn = new JButton("Remove Item");
        styleButton(removeItemBtn);
        removeItemBtn.addActionListener(e -> removeItemFromCurrent());
        buttonPanel.add(removeItemBtn);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "ListCharacters"));
        buttonPanel.add(backBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "ViewCharacter");
    }

    private void updateViewDetails() {
        if (currentViewedChar == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Details: ").append(currentViewedChar).append("\n\n");
        sb.append("Inventory:\n");
        List<Item> items = currentViewedChar.getInventory().getItems();
        if (items.isEmpty()) {
            sb.append("  (empty)\n");
        } else {
            for (Item it : items) {
                sb.append("  ").append(it).append("\n");
            }
        }
        sb.append("\nSkills:\n");
        for (Skill s : currentViewedChar.getSkills()) {
            sb.append("  ").append(s).append("\n");
        }
        viewDetailsArea.setText(sb.toString());
    }

    private void addSkillToCurrent() {
        if (currentViewedChar == null) return;
        String sn = JOptionPane.showInputDialog(this, "Skill name:", "Add Skill", JOptionPane.PLAIN_MESSAGE);
        if (sn == null || sn.trim().isEmpty()) return;
        String pStr = JOptionPane.showInputDialog(this, "Power:", "Add Skill", JOptionPane.PLAIN_MESSAGE);
        if (pStr == null) return;
        try {
            int p = Integer.parseInt(pStr.trim());
            currentViewedChar.addSkill(sn.trim(), p);
            updateViewDetails();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid power.");
        }
    }

    private void equipWeaponToCurrent() {
        if (currentViewedChar == null) return;
        String[] options = {"Sword", "Bow", "Staff"};
        int choice = JOptionPane.showOptionDialog(this, "Choose weapon:", "Equip Weapon",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == -1) return;
        Weapon w;
        switch (choice) {
            case 0 -> w = new Sword("Custom Sword", 10);
            case 1 -> w = new Bow("Custom Bow", 9);
            case 2 -> w = new Staff("Custom Staff", 11);
            default -> {
                return;
            }
        }
        currentViewedChar.equipWeapon(w);
        updateViewDetails();
    }

    private void addItemToCurrent() {
        if (currentViewedChar == null) return;
        String iid = JOptionPane.showInputDialog(this, "Item ID:", "Add Item", JOptionPane.PLAIN_MESSAGE);
        if (iid == null || iid.trim().isEmpty()) return;
        String iname = JOptionPane.showInputDialog(this, "Item Name:", "Add Item", JOptionPane.PLAIN_MESSAGE);
        if (iname == null || iname.trim().isEmpty()) return;
        String qStr = JOptionPane.showInputDialog(this, "Quantity:", "Add Item", JOptionPane.PLAIN_MESSAGE);
        if (qStr == null) return;
        try {
            int q = Integer.parseInt(qStr.trim());
            currentViewedChar.getInventory().addItem(new Item(iid.trim(), iname.trim(), q));
            updateViewDetails();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    private void removeItemFromCurrent() {
        if (currentViewedChar == null) return;
        String iid = JOptionPane.showInputDialog(this, "Item ID to remove:", "Remove Item", JOptionPane.PLAIN_MESSAGE);
        if (iid == null || iid.trim().isEmpty()) return;
        boolean removed = currentViewedChar.getInventory().removeItem(iid.trim());
        if (removed) {
            updateViewDetails();
        } else {
            JOptionPane.showMessageDialog(this, "Item not found.");
        }
    }

    private void createBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel statusPanel = new JPanel(new GridLayout(1, 2));
        statusPanel.setBackground(Color.DARK_GRAY);

        heroHpLabel = new JLabel();
        styleLabel(heroHpLabel);
        statusPanel.add(heroHpLabel);

        enemyHpLabel = new JLabel();
        styleLabel(enemyHpLabel);
        statusPanel.add(enemyHpLabel);

        panel.add(statusPanel, BorderLayout.NORTH);

        battleLogArea = new JTextArea();
        styleTextArea(battleLogArea);
        JScrollPane scroll = new JScrollPane(battleLogArea);
        scroll.getViewport().setBackground(Color.BLACK);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton attackBtn = new JButton("Attack");
        styleButton(attackBtn);
        attackBtn.addActionListener(e -> performPlayerAction(1));
        buttonPanel.add(attackBtn);

        JButton skillBtn = new JButton("Use Skill");
        styleButton(skillBtn);
        skillBtn.addActionListener(e -> performPlayerAction(2));
        buttonPanel.add(skillBtn);

        JButton runBtn = new JButton("Run");
        styleButton(runBtn);
        runBtn.addActionListener(e -> performPlayerAction(3));
        buttonPanel.add(runBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "Battle");
    }

    private void startBattle(Character hero) {
        this.battleHero = hero;
        this.battleEnemy = new Warrior("EN-01", "Goblin", new Sword("Rusty", 5));
        battleEnemy.addSkill("Bite", 4);
        hpHero = 100 + hero.getLevel() * 10;
        hpEnemy = 60 + battleEnemy.getLevel() * 8;
        updateBattleStatus();
        battleLogArea.setText(hero.getName() + " vs " + battleEnemy.getName() + "\n");
        cardLayout.show(mainPanel, "Battle");
    }

    private void updateBattleStatus() {
        heroHpLabel.setText("Your HP: " + hpHero);
        enemyHpLabel.setText("Enemy HP: " + hpEnemy);
    }

    private void performPlayerAction(int act) {
        if (hpHero <= 0 || hpEnemy <= 0) return;
        String msg;
        if (act == 3) {
            msg = "You ran.";
            battleLogArea.append(msg + "\n");
            cardLayout.show(mainPanel, "MainMenu");
            return;
        }
        int dmg;
        if (act == 2 && !battleHero.getSkills().isEmpty()) {
            Skill s = battleHero.getSkills().get(rnd.nextInt(battleHero.getSkills().size()));
            dmg = s.getPower() + battleHero.getLevel();
            msg = "You used skill " + s.getName() + " => " + dmg + " dmg";
        } else {
            dmg = battleHero.attack();
            msg = "You attack => " + dmg + " dmg";
        }
        hpEnemy -= dmg;
        battleLogArea.append(msg + "\n");
        updateBattleStatus();
        if (hpEnemy <= 0) {
            msg = "Enemy defeated!";
            battleLogArea.append(msg + "\n");
            battleHero.addXp(50);
            cardLayout.show(mainPanel, "MainMenu");
            return;
        }
        // Enemy turn
        int ed = battleEnemy.attack();
        msg = "Enemy attacks => " + ed + " dmg";
        hpHero -= ed;
        battleLogArea.append(msg + "\n");
        updateBattleStatus();
        if (hpHero <= 0) {
            msg = "You were defeated.";
            battleLogArea.append(msg + "\n");
            cardLayout.show(mainPanel, "MainMenu");
        }
    }

    private void handleSave() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fn = fc.getSelectedFile().getPath();
            try {
                gm.saveRoster(fn);
                JOptionPane.showMessageDialog(this, "Saved to " + fn);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        }
    }

    private void handleLoad() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fn = fc.getSelectedFile().getPath();
            try {
                gm.loadRoster(fn);
                updateCharModel();
                JOptionPane.showMessageDialog(this, "Loaded " + gm.getRoster().size() + " characters.");
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CursedKingdom::new);
    }
}

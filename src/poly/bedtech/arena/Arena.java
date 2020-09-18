package poly.bedtech.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import poly.bedtech.MinGame;
import poly.bedtech.weapons.CustomWeapon;
import poly.bedtech.weapons.WeaponManager;

public class Arena {

	//must be UNIQUE
	public String name;
	
	//devrait etre privee pour saveConfig a chaque fois
	public Location loc1;
	public Location loc2;
	
	//mettre Equipes
	
	public boolean isOpen = false;
	public boolean isStarted = false;
	
	public World world;
	
	//dans autre classe ?

	public BukkitRunnable borderRun;
	
	//default inventory .. don't modify except config
	public CustomWeapon weapon;
	
	public List<Player> players = new ArrayList<Player>();
	
	//false is spectator and true is inGame
	public List<Boolean> isInGame = new ArrayList<Boolean>();
	
	public int minPlayer = 2;
	public int maxPlayer = 99;
	
	public List<Location> spawnLocs;
	public Location specLoc;
	
	
	public Arena(String name, World world) {
		
		this.name = name;
		this.world = world;
		
		spawnLocs = new ArrayList<Location>();
		
		if (WeaponManager.weapons.size() == 0) {
			System.err.println("no weapon found");
			return;
		}
		weapon = WeaponManager.weapons.get(0);
		
	}
	
	public Arena(String name, Location l1, Location l2, World world) {
		this.name = name;
		this.loc1 = l1;
		this.loc2 = l2;
		
		this.world = world;
		
		spawnLocs = new ArrayList<Location>();
		
		if (WeaponManager.weapons.size() == 0) {
			System.err.println("no weapon found");
			return;
		}
		weapon = WeaponManager.weapons.get(0);
		
	}
	
	public void changeBorder() {
		
		if (borderRun == null)
			showBorder();
		else
			unShowBorder();
		
	}
	
	private void unShowBorder() {
		borderRun.cancel();
		borderRun = null;
	}
	
	
	
	//Todo fix error
	private void showBorder() {
		borderRun = new BukkitRunnable() {
			@Override
			public void run() {
				
				int startX = Math.min(loc1.getBlockX(), loc2.getBlockX());
				int startY = Math.min(loc1.getBlockY(), loc2.getBlockY());
				int startZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
				int endX = Math.max(loc1.getBlockX(), loc2.getBlockX());
				int endY = Math.max(loc1.getBlockX(), loc2.getBlockY());
				int endZ = Math.max(loc1.getBlockX(), loc2.getBlockZ());
				
				for (double x = startX + 0.5; x <= endX + 1; x++) {
		            for (double y = startY; y <= endY + 1; y++) {
		                for (double z = startZ + 0.5; z <= endZ + 1; z++) {
		                        if ((int) x == startX || (int) x == endX || 
		                            (int) y == startY || (int) y == endY + 1|| 
		                            (int) z == startZ || (int) z == endZ) {
		                	        //loc1.getWorld().spawnParticle(Particle.REDSTONE, (float) x, (float) y,(float) z, 1, new Particle.DustOption(Color.RED,1));
		                        	loc1.getWorld().spawnParticle(Particle.BARRIER, (float) x, (float) y,(float) z, 1);
		                        	/*
		                            Object packet = Reflections.getPacket("PacketPlayOutWorldParticles",
		                                    "reddust", (float) x, (float) y, (float) z,
		                                    0f, 0f, 0f, 0f, 1);
		                            Reflections.sendPacket(p, packet);
		                            */
		                      }
		                }
		             }
		         }
				
			}
			
			
			
		};
		borderRun.runTaskTimer(MinGame.INSTANCE, 0, 20);
		//Bukkit.getScheduler().runTaskTimer(LimaMain.INSTANCE, borderRun,0,20);
		
		
	}
	
	public String getName() {
		return name;
	}
	
	public void saveConfig(MinGame limInstance) {
		
		System.out.println("We save the config");
		
		String def = "arenas."+name+".";
		
		limInstance.getConfig().set(def+"name", this.name);
		
		limInstance.getConfig().set(def+"world", this.world.getName());
		
		if (loc1 != null) {
			limInstance.getConfig().set(def+"loc1.x", loc1.getX());
			limInstance.getConfig().set(def+"loc1.y", loc1.getY());
			limInstance.getConfig().set(def+"loc1.z", loc1.getZ());
		}
		if (loc2 != null) {
			limInstance.getConfig().set(def+"loc2.x", loc2.getX());
			limInstance.getConfig().set(def+"loc2.y", loc2.getY());
			limInstance.getConfig().set(def+"loc2.z", loc2.getZ());
		}

		if (specLoc != null) {
			limInstance.getConfig().set(def+"specLoc.x", specLoc.getX());
			limInstance.getConfig().set(def+"specLoc.y", specLoc.getY());
			limInstance.getConfig().set(def+"specLoc.z", specLoc.getZ());
		}
		
		limInstance.getConfig().set(def+"weapon", weapon.localizedName);
		
		limInstance.getConfig().set(def+"minPlayer", minPlayer);
		limInstance.getConfig().set(def+"maxPlayer", maxPlayer);
		
		limInstance.getConfig().set(def+"isOpen", isOpen);
		
		limInstance.getConfig().set(def+"spawnlocs", null); 
		
		for(int i=0;i<spawnLocs.size();i++) {
			
			Location l = spawnLocs.get(i);
			limInstance.getConfig().set(def+"spawnlocs."+i+".x", l.getX());
			limInstance.getConfig().set(def+"spawnlocs."+i+".y", l.getY());
			limInstance.getConfig().set(def+"spawnlocs."+i+".z", l.getZ());
		}
		
		limInstance.saveConfig();
		
	}
	
	private List<Player> getAllPlayers(){
		
		return players;
		
		
	}
	
	
	public boolean canStartGame() {
		if (players.size() < minPlayer)
			return false;
		
		return true;
		
	}
	
	public static void removeInexistentPlayer(Arena self) {
		
		//should not happen
		if (self.players.size() != self.isInGame.size()) {
			System.err.print("Err, player size different than spec size");
			self.players.clear();
			self.isInGame.clear();
		}
		
		for(int i=0;i<self.players.size();i++) {
			if (self.players.get(i) == null) {
				self.players.remove(i);
				self.isInGame.remove(i);
			}
		}
	}
	
	public void tryBeginGame() {
		
		
		
		
		Arena self = this;
		borderRun = new BukkitRunnable() {
			int count = 10;
			
			@Override
			public void run() {
		
				if (!canStartGame()) {
					this.cancel();
					return;
				}
				
				removeInexistentPlayer(self);
			
				
				for(Player p : self.getAllPlayers()) {
					p.sendMessage("game begin in :"+count);
				}
				count--;
				if (count == 0) {
					self.startGame();
					this.cancel();
				}
			}
			
			
			
		};
		borderRun.runTaskTimer(MinGame.INSTANCE, 0, 20);
		
	}
	//https://bukkit.org/threads/saving-custom-inventory.474847/
	private void startGame() {
		
		//prendre en compte le fait qu'un joueur peut d�co avant le lancement
		
		removeInexistentPlayer(this);
		
		isStarted = true;
		
		ItemStack item = weapon.getItem();
		
		int j = 0;
		for(int i=0;i<players.size();i++) {
			Player p = players.get(i);
			
			p.sendMessage("GAME STARTED");
			
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setFireTicks(0);
            
            savePlayerData(p);
            
			p.teleport(spawnLocs.get(j));
			
			p.getInventory().clear();
			p.getInventory().addItem(item);
			
			isInGame.set(i, true);
			
			j++;
			if (j >= spawnLocs.size())
				j= 0;
		}
		
	}
	
	private void savePlayerData(Player p) {
		
		MinGame limInstance = MinGame.INSTANCE;
		limInstance.getConfig().set("players."+p.getName()+".inventory.content", p.getInventory().getContents());
		Location l = p.getLocation();
		limInstance.getConfig().set("players."+p.getName()+".loc.x", l.getX());
		limInstance.getConfig().set("players."+p.getName()+".loc.y", l.getY());
		limInstance.getConfig().set("players."+p.getName()+".loc.z", l.getZ());
		limInstance.getConfig().set("players."+p.getName()+".loc.world", l.getWorld().getName());
		
		
		limInstance.saveConfig();
	}
	
	
	public void joinArena(Player player) {
		
		if (!isOpen) {
			player.sendMessage("The arena is not open");
		}
		if (isStarted) {
			player.sendMessage("The game already started");
		}
		
		if (players.contains(player)) {
			player.sendMessage("You are already in a team");
			return;
		}
		
		players.add(player);
		isInGame.add(false);
		
		player.sendMessage("arena joined");
		tryBeginGame();
		
	}
	
	
	private void bringBack(Player p) {
		
		System.out.println("we bring back :"+p.getName());
		
		MinGame limInstance = MinGame.INSTANCE;
		
		ItemStack[] contents = (ItemStack[]) MinGame.INSTANCE.getConfig().get("players."+p.getName()+".inventory.content");
		if (contents == null) {
			p.sendMessage("Sorry, Can't retrieve your stuff");
		}else {
			p.getInventory().setContents(contents);	
		}
		
		if (limInstance.getConfig().contains("players."+p.getName()+".loc.x")) {
				
			double x = limInstance.getConfig().getDouble("players."+p.getName()+".loc.x");
			double y = limInstance.getConfig().getDouble("players."+p.getName()+".loc.y");
			double z = limInstance.getConfig().getDouble("players."+p.getName()+".loc.z");
			String w = limInstance.getConfig().getString("players."+p.getName()+".loc.world");
			
			if (x == 0 && y == 0 && z == 0) {
				p.sendMessage("Sorry, can't find your old location");
			}
			if (w == null || w == "")
				w = "world";
			
			World world = limInstance.getServer().getWorld(w);
			if (world == null) {
				System.err.println("Error, world :"+w+", not found for :"+p.getName());
				p.sendMessage("Error, old world not found");
			}
			else {
				Location l = new Location(world,x,y,z);
				p.teleport(l);
			}
			
			limInstance.getConfig().set("players."+p.getName(), "");
		}else {
			p.sendMessage("Sorry didn't find your data");
		}
		
		limInstance.saveConfig();
		
	}
	
	public void leaveArena(Player player, boolean byCommand) {
		
		if (players.contains(player)) {
			System.out.println("leaved");
			int index = players.indexOf(player);
			
			players.remove(index);
			isInGame.remove(index);
			
			//if doesn't come back from endGame
			bringBack(player);
			
			if (byCommand)
				checkGameEnded();
			
		}
	}

	
	private boolean checkGameEnded() {
		
		if (!isStarted)
			return false;
		
		int count = 0;
		Player winner = null;
		for(int i=0;i<players.size();i++) {
			if (isInGame.get(i)) {
				count++;
				winner = players.get(i);
			}
		}
		if (count == 1) {
			congratWinner(winner);
			endGame();
			return true;
		}else if (count == 0) {
			endGame();
			return true;
		}
		return false;
	}
	
	public void endGame() {
		
		System.out.println("end game :"+players.size());
		
		for(Player p : getAllPlayers()) {
			bringBack(p);
		}
		
		isStarted = true;
		players.clear();
		isInGame.clear();
	}
	
	private void congratWinner(Player winner) {

		MinGame.INSTANCE.getServer().broadcastMessage("Bravo a :"+winner.getName());
		
	}
	
	public void goSpec(Player player) {
		
		if (players.contains(player)) {
			if (specLoc == null)
				leaveArena(player,false);
			else {
				isInGame.set(players.indexOf(player),false);
				player.teleport(specLoc);
			}
		}
		checkGameEnded();

	}
	
	public int getNumberPlayerNeeded() {
		
		System.out.println("min player : "+minPlayer);
		System.out.println("player size :" +players.size());
		
		int res = minPlayer - players.size();
		if (res < 0)
			return 0;
		else
			return res;
		
	}

	
	
}

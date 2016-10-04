//uncomment #legitCode for more functionality

package jc;
import java.awt.Graphics2D;

import java.awt.geom.Point2D;

import java.util.*;
import robocode.*;
import robocode.util.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/** 
 * Robie - a robot by (your name here)
 */
public class PatternMatch extends AdvancedRobot
{
	double targetX=200;
	
	double x=0,xlast=0;
	double dx=0;
	
	double a=.5, b=.3;
	
	int fo=1;
	
	double gf, gfsum;

	double radarTurn=Math.PI*2,gunTurn,p=1;
	
	double linTargAngle;
	

	Enemy enemy=new Enemy(this);
	
	
	int windowSize = 100;	
	LinkedList<Enemy> enemyPrevStates = new LinkedList<Enemy>();
	
	Enemy[] enemyPatternProjNextStates = new Enemy[0];
	
	Enemy[] enemyLastMatchingWindow = new Enemy[0];	

	
	static int bestBucket=0,bestBucketVal=0;
	static int numBuckets=7;
	static int numDataPts=50;
	static Queue<Wave> hitQ=new LinkedList<Wave>();
	static int[] hitBuckets=new int[numBuckets];
	
	Vector<Wave> bullets=new Vector<Wave>();
	/**
	Classifiers | Misses
	feature space
		dataPts=100,	continuous		
		dataPts=100,	intermittent
		
		dataPts=50,	continuous		
		dataPts=50,	intermittent
		
		dataPts=10,	continuous		
		dataPts=10,	intermittent

		dataPts=3,	continuous		
		dataPts=3,	intermittent
		

	*/
	
	/**
	 * run: Robie's default behavior
	 */
	public void run() {
	
//		System.out.println(Arrays.toString(outRay));

		//mov = new RobieSurfer(this);
		//targ = new RobieSailor(this);
		
		//reportHit(new Wave(0,0));
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		while(true) {
			
			setTurnRadarRightRadians(radarTurn);	
			radarTurn=Math.PI*2; //will be overridden by scan
			

		// Replace the next 4 lines with any behavior you would like
			

			//movement
			x=getX()-targetX;
			xlast=x;
			dx=x-xlast;
			
			double dbear=enemy.absbear-getHeadingRadians();
			double dd=enemy.dist-400;
			if(dd>200) dd=200;
			if(dd<-200) dd=-200;
			
			double dh=0;//Utils.normalAbsoluteAngle(getHeadingRadians()-enemy.head);
			if(dh>Math.PI/6) dh=Math.PI/6;
			if(dh<-Math.PI/6) dh=-Math.PI/6;
			//setTurnRightRadians(fo*Utils.normalRelativeAngle(a*x+b*dx));
			setTurnRightRadians(Utils.normalRelativeAngle(dbear+Math.PI/2-dd/200*Math.PI/6*-1*fo+dh));
			if(Math.random()<.02 || Math.abs(getDistanceRemaining())<=0) {
				fo=-1*fo;
				setAhead(-1*fo*10000);
			}
			
			//shooting
			
				//bullets.add(new Wave(getX(),getY(),p));
			if(getGunHeat()<=0) {
				
				setFire(p);
				//bullets.add(new Wave(getX(),getY(),p)); //#legitCode
				
			}
			
//guessfactor targeting
			//gunTurn=Utils.normalRelativeAngle(enemy.absbear-getGunHeadingRadians())+gf*Math.asin(8.0/Vb(p));	

//linear targeting
			gunTurn=Utils.normalRelativeAngle(linTargAngle-getGunHeadingRadians());	

//pattern matchter targeting
			if(enemyPrevStates.size()>=windowSize) {
				
				
				Enemy[] last7 = enemyPrevStates.subList(0,windowSize).toArray(new Enemy[0]);
				Enemy[] past = enemyPrevStates.toArray(new Enemy[0]);
				
				
				double bestScore=0;
				int bestScoreI=past.length-1;
				
				for(int i=windowSize;i<past.length-windowSize;i++) {
					
					double score = 0;
					//comp last 7 to appropriate window
					for(int j=0;j<windowSize; j++) {
						score += last7[j].diffScore(past[i+j]);
					}
					
					if(score>bestScore) {
						bestScore=score;
						bestScoreI=i;
					}
				
				}
				
				System.out.println(bestScoreI + " score: " + bestScore);
				
				enemyLastMatchingWindow = Arrays.copyOfRange(past, bestScoreI, Math.min(past.length,bestScoreI+windowSize-1)); 
                                enemyPatternProjNextStates = Arrays.copyOfRange(past, 0, bestScoreI);

                                
                                    System.out.println("dhead " + enemy.dHead);

                                Enemy currEnemy = enemy.clone();
                                
                                //0 is futuremost
                                for(int i=enemyPatternProjNextStates.length-1;i>=0;i--) {
                                    
                                    currEnemy.advanceByDiff(enemyPatternProjNextStates[i]);
                                    enemyPatternProjNextStates[i]=currEnemy.clone();
                                    
                                }
				
                                
                                
			//	for(Enemy state;enemyPatternProjNextStates) {
					//todo
				//}
			
			}
			
			gunTurn=Utils.normalRelativeAngle(linTargAngle-getGunHeadingRadians());	
                        
                        
                        

			setTurnGunRightRadians(gunTurn);	

                        
                        
                        
			for (Iterator<Wave> i=bullets.iterator();i.hasNext();) {
				Wave w=i.next();
				if(w.inactive) {
					//System.out.println("reauoeauaoeu");
				
					i.remove();
					continue;
				}
				
				w.nextTick();
			}
			execute();
		}
	}


	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		enemy.update(e);
		enemyPrevStates.addFirst(enemy.clone());
		if(enemyPrevStates.size() > 500)
			enemyPrevStates.removeLast();
		

		double beta = Math.PI-enemy.head + enemy.absbear;
		double alpha = Math.asin(enemy.v * Math.sin(beta)/Vb(p));
		linTargAngle=enemy.absbear + alpha;
		
		radarTurn=(Utils.normalRelativeAngle(enemy.absbear-getRadarHeadingRadians()));	
	
		
		
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		
	}	

	public void onPaint(Graphics2D g) {
		g.setColor(java.awt.Color.RED);
//		g.drawLine((int)targetX,  0,      (int) targetX,                             500);
		 
		//#legitCode
		//g.drawLine( (int) (getX()), (int) (getY()), (int)( getX() + enemy.dist*Math.sin(linTargAngle) ) , (int) ( getY() + enemy.dist*Math.cos(linTargAngle) ) );                           
		  


			enemy.paint(g);
			
			
			for (Wave w:bullets) {
					w.paint(g);
			}
  		   // Paint a filled rectangle at (50,50) at size 100x150 pixels
  		   //g.fillRect(50, 50, 100, 150);
		// Replace the next line with any behavior you would like
		g.setColor(java.awt.Color.RED);
		int i=0;
		for (Enemy pastEnemy: enemyPrevStates) {
                   
					if(i<windowSize)
						g.setColor(java.awt.Color.YELLOW);
					else
						g.setColor(java.awt.Color.RED);
                                        
					g.drawOval( (int) pastEnemy.x,(int) pastEnemy.y,10,10);
					i++;
			}
			
		for(Enemy futureEnemy: enemyLastMatchingWindow) { 
                     if(futureEnemy==null) {
                        System.out.println("null futureEnemy");
                        continue;
                    }
			g.setColor(java.awt.Color.GREEN);
			g.drawOval( (int) futureEnemy.x, (int) futureEnemy.y, 10, 10);
		}		
                
		for(Enemy futureEnemy: enemyPatternProjNextStates) { 
			g.setColor(java.awt.Color.ORANGE);
			g.drawOval( (int) futureEnemy.x, (int) futureEnemy.y, 10, 10);
		}
		
	}	

/*	int bucket(double gf) {
		return (int)Math.round(gf*numBuckets.length);
	}*/
	
	//get bullet vel from pow
	public double Vb(double p) {
		return 20 - 3 * p;
	}

	public void reportHit(Wave hit) {
		
		hitQ.offer(hit);
		
		hitBuckets[hit.hitBucket]++;

		/*	System.out.print('|');
			int gi=0;
			for(gi=0;gi<hit.hitBucket;gi++) {
				//	if(gi==3)
					//	System.out.print('|');
				//	if(gi==hit.hitBucket-1)
					System.out.print("xx");	
				//	else
						//System.out.print(" ");		
			}
			/*for(int i=gi;i<numBuckets;i++) {
				if(i==3)
					System.out.print('|');
			
				System.out.print("  ");		
			}*/
		//	System.out.println();
			
		if(hitQ.size()>numDataPts) {
			--hitBuckets[hitQ.poll().hitBucket];
		}
		
		
		int w=0,v=0;
		bestBucket=0;
		bestBucketVal=0;
		for(w=0;w<hitBuckets.length;w++) {
			


			v=hitBuckets[w];
			
			System.out.print('|');
			for(int i=0;i<v;i++) {
					System.out.print('x');		
			}
			System.out.println();

			if(v>bestBucketVal) {
				bestBucket=w;
				bestBucketVal=v;
			}
		}
		
		gf=bestBucket*2.0/(hitBuckets.length-1)-1;
	
		
		System.out.println("Using GF "+gf + " " + bestBucket);
		//System.out.println("BB "+bestBucket + " GF "+ gf + " | " + bullets.size());
	}
	
	public int whichBucket(double gf) {
		return (int)((gf+1+.000000001)/2*numBuckets);
	}	




class Wave extends Point2D.Double {
		
	double p,v,r,initAng,maxAng;
	int hitBucket;
	
	boolean inactive=false;
	public Wave(double x, double y,double p) {
		this.x=x;
		this.y=y;
		this.p=p;
		this.v=Vb(p);
		this.r=0;
		this.maxAng=Math.asin(8.0/Vb(p));
		this.initAng=Math.atan2(enemy.x-x,enemy.y-y);
			
	}
	
	public void nextTick() {
		
		if(inactive)
			return;
			
		r+=v;
		
	//	System.out.println("M " + this.distance(enemy) + " | " +( r-getWidth()/2));
		if(isHit()) {
			//System.out.println("hit!");
			double hitgf=(Math.atan2(enemy.x-x,enemy.y-y)-initAng)/maxAng;
			hitBucket=whichBucket(hitgf);
			if(hitBucket>=0 && hitBucket <7) //otherwise myBot hit a wall or sthing odd
				reportHit(this);	
			
			
			
			inactive=true;	
			
			//gf=.8*gf+.2*hitgf;
//			reportHit(gf);
		}
	}
	
	public boolean isHit() {
		if(this.distance(enemy)<r-getWidth()/2) {
			
			//System.out.println("IS HIT " + this.distance(enemy) + " | " +( r-getWidth()/2));
			return true;
		}		
		else
			return false;
	}
	
	public void paint(Graphics2D g) {
		//if(inactive) return;
	
		g.setColor(java.awt.Color.GREEN);
		if(inactive) g.setColor(java.awt.Color.ORANGE);
		
		
		g.drawOval((int)(x-r),  (int)(y-r),      (int) r*2,(int) r*2);
		double t=5;
		for(int i=-numBuckets/2;i<=numBuckets/2;i++) {
			/*
			//if(inactive)
				if(2*i+hitBucket==0)		
					g.setColor(java.awt.Color.YELLOW);
				else
					g.setColor(inactive ? java.awt.Color.ORANGE : java.awt.Color.GREEN );
			*/
			g.drawLine((int)(x+Math.sin(initAng+maxAng/(numBuckets/2)*i)*(r-t)),(int)(y+Math.cos(initAng+maxAng/(numBuckets/2)*i)*(r-t)),(int)(x+Math.sin(initAng+maxAng/(numBuckets/2)*i)*(r+t)),(int)(y+Math.cos(initAng+maxAng/(numBuckets/2)*i)*(r+t)));
//			g.drawLine((int)x,(int)y,(int)(x+Math.sin(initAng+maxAng/(numBuckets/2)*i)*r),(int)(y+Math.cos(initAng+maxAng/(numBuckets/2)*i)*r));
		}
	}	
}


class Enemy extends Point2D.Double {
		
	double absbear,dist,head,v;
	AdvancedRobot myBot;
	
	double dHead;
	
	
	public Enemy(AdvancedRobot bot) {
		this.myBot=bot;
	
	/*	this.x=x;
		this.y=y;
		this.p=p;
		this.v=Vb(p);
		this.r=0;
		this.maxAng=Math.asin(8.0/Vb(p));
		this.initAng=Math.atan2(enemy.x-x,enemy.y-y);*/
			
	}
	public void update(ScannedRobotEvent e) {
		
		//lastScan=e;
		
		
		absbear=getHeadingRadians()+e.getBearingRadians();
		dist=e.getDistance();

		dHead=e.getHeadingRadians()-head;
		head=e.getHeadingRadians();
		
		
		v=e.getVelocity();
		
		x=myBot.getX()+Math.sin(absbear)*dist;
		y=myBot.getY()+Math.cos(absbear)*dist;
		
	}
	
	/*
	public void nextTick() {
		r+=v;
		
		if(this.distance(enemy)>r-getWidth()) {
			double hitgf=(Math.atan2(enemy.x-x,enemy.y-y)-initAng)/maxAng;
			reportHit(hitgf);		
			System.out.println(numBuckets);
			gf=.8*gf+.2*hitgf;
//			reportHit(gf);
		}
	}
	*/
	public void paint(Graphics2D g) {
		g.setColor(java.awt.Color.RED);
		g.drawRect((int)(x-getWidth()/2),  (int)(((y-getHeight()/2))), (int)(getWidth()),  (int)(getHeight()));
		
	}	
	
	public Enemy clone() {
		Enemy stamp = new Enemy(myBot);
	
		stamp.x=x;
		stamp.y=y;
		stamp.absbear=absbear;
		stamp.dist=dist;
		stamp.head=head;
		stamp.v=v;
                
                stamp.dHead=dHead;
		
		return stamp;
	}
	
	public double diffScore(Enemy e) {
		return Math.abs(v-e.v) + Math.abs(dHead-e.dHead);
	}
        
        
        
	public void advanceByDiff(Enemy diff) {
            
            
                x+=v*Math.sin(head);
                y+=v*Math.cos(head);
                
                
                head+=diff.dHead;
                dHead=diff.dHead;
                
                v=diff.v;
                
                dist=this.distance(myBot.getX(), myBot.getY());
                absbear=Math.atan2(x-myBot.getX(), y-myBot.getY());
                
                
                
                
        }


    }

}



																																																																																																																																																																																																																																																													
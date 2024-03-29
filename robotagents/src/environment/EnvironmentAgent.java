package environment;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;

import utils.*;
import ontologies.*;
import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.Codec.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.leap.LEAPCodec;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
/**
 * Agent responsible for simulating Environment in which Agents act 
 * 
 */
public class EnvironmentAgent extends Agent implements RobotsVocabulary
{
   protected ArrayList<EnvObject> objects = new ArrayList<EnvObject>();
   protected ArrayList<EnvObject> robots = new ArrayList<EnvObject>();
   protected Codec codec = new LEAPCodec();
   protected Ontology ontology = RobotsOntology.getInstance();
   protected int sightRange = 20;
   protected MessageTemplate mt = MessageTemplate.and(MessageTemplate
         .MatchLanguage(codec.getName()), MessageTemplate
         .MatchOntology(ontology.getName()));

   protected ActionFrame controlPanel;

   protected class SendBehav extends OneShotBehaviour
   {
      protected ArrayList<EnvObject> neighborhood = new ArrayList<EnvObject>();
      protected int x, y, recid;
      protected AID id;

      public SendBehav(AID nid, int rid, int nx, int ny)
      {
         id = nid;
         x = nx;
         y = ny;
         recid = rid;
         for (int i = 0; i < objects.size(); i++)
            if ((objects.get(i).getPosX() - x) * (objects.get(i).getPosX() - x)
                  + (objects.get(i).getPosY() - y)
                  * (objects.get(i).getPosY() - y) < Globals.SIGHT_RANGE * Globals.SIGHT_RANGE)
               neighborhood.add(objects.get(i));

         for (int i = 0; i < robots.size(); i++)
            if ((robots.get(i).getPosX() - x) * (robots.get(i).getPosX() - x)
                  + (robots.get(i).getPosY() - y)
                  * (robots.get(i).getPosY() - y) < Globals.SIGHT_RANGE * Globals.SIGHT_RANGE)
               neighborhood.add(robots.get(i));

      }

      public void action()
      {
         for (int i = 0; i < neighborhood.size(); i++)
         {
            EnvObject tmp = neighborhood.get(i);
            MessageInfo m = new MessageInfo(0, recid, 0, 0, (float) 1000.0);
            m.setF(new Fact(tmp.getId(), tmp.getPosX(), tmp.getPosY(),
                  new Date()));
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(id);
            msg.setLanguage(codec.getName());
            msg.setOntology(ontology.getName());
            msg.setConversationId("forYourEyesOnly");
            try
            {
               getContentManager().fillContent(msg, m);
               send(msg);
            }
            catch (CodecException ce)
            {
               ce.printStackTrace();
            }
            catch (OntologyException oe)
            {
               oe.printStackTrace();
            }
         }
      }
   }



   protected class EnvUpdateBehav extends CyclicBehaviour
   {
      public void action()
      {
         ACLMessage msg = receive(mt);
         if (msg != null)
         {
            if (msg.getPerformative() == ACLMessage.REQUEST)
            {
               try
               {
                  ContentElement content = getContentManager().extractContent(
                        msg);
                  MessageInfo info = (MessageInfo) content;
                  Fact fact = info.getF();

                  for (int i = 0; i < robots.size(); i++)
                     if (robots.get(i).getId() == fact.getId())
                        robots.remove(i);
                  robots.add((EnvObject) fact);

                  controlPanel.upgrade(objects, robots);
                  controlPanel.repaint();

                  addBehaviour(new SendBehav(msg.getSender(), fact.getId(),
                        fact.getPosX(), fact.getPosY()));

                  /*
                  System.out.println("OBJECTS:");
                  for(EnvObject ob : objects)
                     System.out.println(ob.getId() + " " + ob.getPosX() + " " + ob.getPosY());

                  System.out.println("ROBOTS:");

                  for(EnvObject ob : robots)
                     System.out.println(ob.getId() + " " + ob.getPosX() + " " + ob.getPosY());
                  */
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }

            }

         }
         else
         {
            block();
         }
      }
   }

   public void setup()
   {
      getContentManager().registerLanguage(codec);
      getContentManager().registerOntology(ontology);

      ServiceDescription sd = new ServiceDescription();
      sd.setType("ENVIRONMENT");
      sd.setName(getLocalName());

      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(getAID());
      dfd.addServices(sd);

      try
      {
         DFService.register(this, dfd);
      }
      catch (FIPAException fe)
      {
         fe.printStackTrace();
      }

      controlPanel = new ActionFrame(500, 500, robots, objects);
      controlPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      controlPanel.show();

      addBehaviour(new EnvUpdateBehav());
   }

   protected void takeDown()
   {
      try
      {
         DFService.deregister(this);
      }
      catch (Exception e)
      {
      }
   }

}

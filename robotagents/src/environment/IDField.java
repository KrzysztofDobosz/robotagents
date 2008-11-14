package environment;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * Text field to get new objectID
 * @author Maciej Kupczak
 *
 */
public class IDField extends JTextField
{
   /**
    * Creates 
    * @param domwar domy�lna warto�c, jezeli d�u�sza ni� 4 czyfry to przycinana do 4 pierwszych
    * @param rozmiar rozmiar pola tekstowego
    */
   public IDField(int size)
   {
      super(size);
      setInputVerifier(new WerLength(size));
   }
   
   protected Document createDefaultModel()
   {
      return new IdDoc();
   }
   /**
    * sprawdza czy zawartosc pola jest poprawna liczba calkowita czterocyfrowa
    * @return true jesli jest poprawna
    */
   public boolean jestPoprawny()
   {
      return IdDoc.poprawny(getText());
   }
   /**
    * Pobiera wartosc liczbowa pola tekstowwego
    * @return liczba wpisana w polu, 1337 jesli zawartosc jest nie poprawna
    */
   public Integer pobierzWartosc()
   {
      try
      {
         return Integer.parseInt(getText());
      }
      catch(NumberFormatException w)
      {
         return null;
      }
   }
}

/**
 * Weryfikator, kt�ry sprawdza, czy zawartosc pola tekstowego jest poprawna liczb� ca�kowit�
 * @author Maciek
 *
 */
class WerLength extends InputVerifier
{
   private int size;
   
   public WerLength(int s)
   {
      size = s;
   }
   
   public boolean verify(JComponent komponent)
   {
      String tekst = ((JTextComponent)komponent).getText();
      if (tekst.length()>size)
         return false;
      else
         return IdDoc.poprawny(tekst);     
   }
}

/**
 * Dokument kt�ry moze przechowywa tylko 4 cyfrowe integery
 * @author Maciej Kupczak
 */
class IdDoc extends PlainDocument
{
   
   public void insertString(int poz, String str, AttributeSet a) throws BadLocationException
   {
      if (str==null) return;
      
      String dawnyString = getText(0, getLength());
      String nowyString = dawnyString.substring(0,poz)+str+dawnyString.substring(poz);
      
      if (poprawny(nowyString))
      {  
         if ((nowyString.length())>4)
         {
            nowyString = nowyString.substring(0, 4);
            super.replace(0, 4, nowyString, a);
         }
         else 
         {
            super.insertString(poz, str, a);
         }
      }
   }
   /**
    * sprawdza czy podany string jest liczb�
    * @param str sprawdzany ci�g
    * @return true je�li jest liczba, false w przeciwnym wypadku
    */
   static boolean poprawny(String str)
   {
      try
      {
         Integer.parseInt(str);
         return true;
      }
      catch(NumberFormatException w)
      {
         return false;
      }
   }
}

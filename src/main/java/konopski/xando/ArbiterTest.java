package konopski.xando;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Data driven tests for Arbiter class.
 * 
 * @author Lukasz Konopski
 */
@Test
public class ArbiterTest
{
	/** null in one letter id */
	public final static Character n = null;
	/** o character */
	public final static Character o = 'o';
	/** x character */
	public final static Character x = 'x';
	
	/**
	 * 	This method will provide data to any test method that declares that its Data Provider
	 * 	is named "fieldsProvider"
	 */
	@DataProvider(name = "fieldsProvider")
	public Object[][] createData1()
	{		
		return new Object[][]
    			    {			
				{
					true,           //finished
					0,              //last move					
					new Character[] //fields
					{ o , o , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					1,              //last move					
					new Character[] //fields
					{ o , o , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					2,              //last move					
					new Character[] //fields
					{ o , o , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					false,           //finished
					1,              //last move					
					new Character[] //fields
					{ n , o , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					false,           //finished
					1,              //last move					
					new Character[] //fields
					{ o , o , n ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					false,           //finished
					0,              //last move					
					new Character[] //fields
					{ o , n , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					false,           //finished
					2,              //last move					
					new Character[] //fields
					{ o , n , o ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					4,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  o , o , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					3,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  o , o , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					5,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  o , o , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					8,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  x , x , n ,
					  o , o , o 
					},
				}
				,
				{
					true,           //finished
					6,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  x , x , n ,
					  o , o , o 
					},
				}
				,
				{
					true,           //finished
					7,              //last move					
					new Character[] //fields
					{ 
					  n , x , o ,
					  x , x , n ,
					  o , o , o 
					},
				}
				,
				{
					false,           //finished
					0,              //last move					
					new Character[] //fields
					{ 
					  x , n , n ,
					  n , n , n ,
					  n , n , n 
					},
				}
				,
				{
					false,           //finished
					8,              //last move					
					new Character[] //fields
					{ 
					  x , n , n ,
					  n , n , n ,
					  n , n , o 
					},
				}
				, 
				{
					false,           //finished
					4,              //last move					
					new Character[] //fields
					{ 
					  x , n , n ,
					  n , x , n ,
					  n , n , o 
					},
				}
				,
				{
					false,           //finished
					5,              //last move					
					new Character[] //fields
					{ 
					  x , n , n ,
					  n , x , o ,
					  n , n , o 
					},
				}
				,
				{
					false,           //finished
					2,              //last move					
					new Character[] //fields
					{ 
					  x , n , x ,
					  n , x , o ,
					  n , n , o 
					},
				}
				,
				{
					false,           //finished
					1,              //last move					
					new Character[] //fields
					{ 
					  x , o , x ,
					  n , x , o ,
					  n , n , o 
					},
				}
				,
				{
					false,           //finished
					3,              //last move					
					new Character[] //fields
					{ 
					  x , o , x ,
					  x , x , o ,
					  n , n , o 
					},
				}

				,
				{
					true,           //finished
					2,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					0,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , o , n ,
					  x , x , o
					},
				}

				,
				{
					true,           //finished
					4,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					4,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , o , n ,
					  x , x , o
					},
				}

				,
				{
					true,           //finished
					6,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , x , o ,
					  x , x , n
					},
				}
				,
				{
					true,           //finished
					8,              //last move					
					new Character[] //fields
					{ o , n , x ,
					  n , o , n ,
					  x , x , o
					},
				}
		};
	}

	/** 
	 * This test method declares that its data should be supplied by the Data Provider 
	 * named "fieldsProvider"
	 */
	@Test(dataProvider = "fieldsProvider")
	public void verify(Boolean expectedResult, Integer lastMove, Character[] fields) 
	{
		System.out.println(expectedResult);
		System.out.println(lastMove);
		System.out.println(Arrays.toString(fields));
		PseudoGame g=new PseudoGame(lastMove,fields);
		Arbiter a=new Arbiter();
		
		assert expectedResult==a.isFinished(g);		
	} 
	
	/**
	 * Helper class that is used by Arbiter to access provided data. 
	 * @author Lukasz Konopski
	 */
	private class PseudoGame implements FieldContainer
	{	
		int lastMove;
		Character[] fields;		
		int size=-1;

		/**
		 * @param lastMove
		 * @param fields
		 */
		public PseudoGame(int lastMove, Character[] fields)
		{
			this.lastMove = lastMove;
			this.fields = fields;
		}

		@Override
		public Character getFields(int index)
		{
			return fields[index];
		}
		
		@Override
		public int getLastMove()
		{
			return lastMove;
		}

		@Override
		public int getSize()
		{
            //lazy ;)
            if(-1==size)
                size=(int) Math.sqrt(fields.length);
            return size;
		}

		@Override
		public void setFields(int index, Character value)
		{
			throw new RuntimeException("it is not expected here");
		}
		
	}
}

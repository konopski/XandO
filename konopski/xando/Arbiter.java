package konopski.xando;

/**
 * Determines if game is finished.
 * This implementation should work even for bigger than 3x3 games (not tested). 
 * @author Łukasz Konopski
 */
public class Arbiter {

    /**
     * @param fc game state holder.
     * @return true if game is finished.
     */
    public boolean isFinished(FieldContainer fc)
    {
        if(checkDiagonals(fc))
            return true;
        if(checkColumn(fc))
            return true;
        if(checkRow(fc))
            return true;
        return false;
    }

    //The below methods could be optimized and merged into one.
    //Separated they are more readable.
    
    /**
     * @param fc game state holder.
     * @return true if column of last move contains only the same characters.
     */
    boolean checkColumn(FieldContainer fc)
    {
        int last=fc.getLastMove();
        Character c=fc.getFields(last);
        assert(null!=c);
        int n = fc.getSize();
        //check column in north direction
        for(int i=last-n;i>=0;i-=n)
        {
            if(null==fc.getFields(i))
                return false;
            if(!c.equals(fc.getFields(i)))
                return false;
        }
        //check column in south direction
        for(int i=last+n;i<n*n;i+=n)
        {
            if(null==fc.getFields(i))
                return false;
            if(!c.equals(fc.getFields(i)))
                return false;
        }
        return true;
    }

    /**
     * @param fc game state holder.
     * @return true if diagonal on which last move is placed all characters are same.
     */
    boolean checkDiagonals(FieldContainer fc) {
    	int last=fc.getLastMove();
    	int n = fc.getSize();    	
    	int col = (last % n);
    	int row = (last / n);
    	if(col == row)
    	{    		
    		//check left diagonal
    		Character c=fc.getFields(last);
            assert(null!=c);

            //need to check right diagonal if not success
            //and last move is common to both diagonals
            boolean retVal=true;
    		for(int i=0;i<n*n;i+=n+1)
    		{
    			if(i==last)
    				continue;
    			if(null==fc.getFields(i))
    			{
    				retVal=false;
    				break;
    			}
                if(!c.equals(fc.getFields(i)))
                {
                	retVal=false;
                	break;
                }
    		}
    		if(retVal)
    			return true;
    	}
    	if(n-col-1 == row)
    	{
        	//check right diagonal    		
    		Character c=fc.getFields(last);
            assert(null!=c);
            for(int i=n*(n-1);i>0;i-=n-1)
            {
            	if(i==last)
    				continue;
            	if(null==fc.getFields(i))
                    return false;
                if(!c.equals(fc.getFields(i)))
                    return false;
            }
            return true;
    	}
    	return false;
    }

    /**
     * @param fc game state holder.
     * @return true if row of last move contains only the same characters.
     */
    boolean checkRow(FieldContainer fc) {
        int last=fc.getLastMove();
        Character c=fc.getFields(last);
        assert(null!=c);
        int n = fc.getSize();
        int col = (last % n);
        //if first field in row
        if(0!=col)
        {
            //check row in west direction
            for(int i=last-1;(i%n)>=0;--i)
            {
                if(null==fc.getFields(i))
                    return false;
                if(!c.equals(fc.getFields(i)))
                    return false;
                if(0==(i%n))
                	break;
            }
        }
        if(n-1!=col)
        {
            //check row in east direction
            for(int i=last+1;(i%n)!=0;++i)
            {
                if(null==fc.getFields(i))
                    return false;
                if(!c.equals(fc.getFields(i)))
                    return false;
            }
        }       
        return true;
    }
}

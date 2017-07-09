package org.apache.polygene.index.sql.support.skeletons;

import org.apache.polygene.spi.query.IndexingException;

@SuppressWarnings( "WeakerAccess" )
public class SqlIndexingException extends IndexingException
{
    public SqlIndexingException( String message )
    {
        super( message );
    }

    public SqlIndexingException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

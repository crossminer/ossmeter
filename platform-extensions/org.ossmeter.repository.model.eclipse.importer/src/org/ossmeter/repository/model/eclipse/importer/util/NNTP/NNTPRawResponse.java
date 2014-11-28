/*******************************************************************************
 * Copyright (c) 2014 OSSMETER Partners.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Davide Di Ruscio - Implementation.
 *******************************************************************************/
//////////////////////////////////////////////////////////////////////////////
//  This file is part of jnntpe, an NNTP client library.
//  Copyright (C) 2001 Scott MacDiarmid
//  
//  This program  is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//////////////////////////////////////////////////////////////////////////////
package org.ossmeter.repository.model.eclipse.importer.util.NNTP;

/**
* NNTPRawResponse is a class that handles returning information to the 
* calling method.  It will return an NNTPError value and a server response
* string if one is available.
*
* @author 	Scott MacDiarmid
* @version 	0.1
* @since    0.1
*/
public class NNTPRawResponse implements NNTPError
{
    // Holds the NNTPError value
    private int m_errorValue;
    // Holds the server response string
    private String m_serverResponse;

    /**
    * Empty constructor
    *
    * @since            0.1
    */
    public NNTPRawResponse()
    {

    }

    /**
    * provides access to the NNTPError value
    *
    * @return       NNTPError value
    * @since        0.1
    */
    public int getErrorValue()
    {
        return m_errorValue;
    }

    /**
    * provides access to the server response string
    *
    * @return       server response string
    * @since        0.1
    */
    public String getServerResponse()
    {
        return m_serverResponse;
    }

    /**
    * stores the error value and response string internally
    *
    * @param errorValue     NNTPError value
    * @param response       server response string
    * @return               none
    * @since                0.1
    */
    public void set(int errorValue, String response)
    {
        m_errorValue = errorValue;
        m_serverResponse = response;
    }
}
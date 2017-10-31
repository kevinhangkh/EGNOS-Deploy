/**
 * *************************************************************************
 * Copyright : Thales Alenia Space
 * Project: EGNOS
 * File: EGNOS_Command_Response.java
 * Date: 20/05/2016
 * Purpose : Contain the command section of a response 
 * Language : Java
 * Author : Kevin HANG
 * History :
 *
 * Version | Date | Name | Change History
 * 01.00 | 20/05/16 | KH | First Creation
 *.
 **************************************************************************
 */
package udp_socket;

public class EGNOS_Command_Response {
    
    byte sectionIdentifier;
    byte[] sectionLength = new byte[2];
    byte[] confirmationKey = new byte[20];
    byte commandID; // never changes
    byte acknowledgementType;
    byte acknowledgementValue;
    
    public EGNOS_Command_Response(byte sId, byte [] sLen, byte [] confirmKey, byte cmdId, byte ackType, byte ackValue) {
        sectionIdentifier = sId;
        sectionLength = sLen;
        confirmationKey = confirmKey;
        commandID = cmdId;
        acknowledgementType = ackType;
        acknowledgementValue = ackValue;
    }
    
    /*****************************************************************************
     * Name : printCmdResp
     * Purpose : Print the Command section of the response
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :.
     * 
     * *************************************************************************/
    public void printCmdResp() {
        System.out.println();
        System.out.println("***COMMAND RESPONSE***");
        System.out.println();
        System.out.printf("sectionID = %02X \n",this.sectionIdentifier);
        System.out.printf("sectionLength :");
        for (int i=0;i<this.sectionLength.length;i++)
        {
            System.out.printf("%02X ",this.sectionLength[i]);
        }
        System.out.println("");
        System.out.printf("confirmationKey :");
        for (int i=0;i<this.confirmationKey.length;i++)
        {
            System.out.printf("%02X ",this.confirmationKey[i]);
        }
        System.out.println("");
        System.out.printf("commandID = %02X \n",this.commandID);
        System.out.printf("acknowledgementType = %02X \n",this.acknowledgementType);
        System.out.printf("acknowledgementValue = %02X \n",this.acknowledgementValue);
        System.out.println("");
    }
    
}

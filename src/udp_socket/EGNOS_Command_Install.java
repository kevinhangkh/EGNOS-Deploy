/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_socket;

/**
 *
 * @author admin
 */
public class EGNOS_Command_Install {
    
    byte sectionIdentifier;
    byte[] sectionLength = new byte[2];
    byte[] confirmationKey = new byte[20];
    byte commandID; // never changes
    byte commandType; // never changes
    byte fileCode;
    byte[] commandParameter = new byte[28];
    int sLen;
    boolean isItRegularCmd = false;
    
    public EGNOS_Command_Install(byte sId, byte [] confirmKey, byte fCode, byte [] cmdParam) {
        sectionIdentifier = sId;
        sectionLength = new byte[] {(byte) 0x00,(byte) computeSectionLength()};
        confirmationKey = confirmKey;
        commandID = 0x00;
        commandType = (byte) 0xc9;
        fileCode = fCode;
        commandParameter = cmdParam;
        isItRegularCmd = true;
    }
    
    /*****************************************************************************
     * Name : computeSectionLength
     * Purpose : Compute the length of this section
     * Argument I/O: 
     * I/O Files: No input file
     * Returns : length of the section
     *
     * @return  *************************************************************************/
    public int computeSectionLength() {
        int sIdLen = 1, cIdLen = 1, cTypeLen = 1, fileCodeLen = 1;
        
        sLen = sIdLen + this.sectionLength.length + this.confirmationKey.length + cIdLen + cTypeLen + fileCodeLen + this.commandParameter.length;
        
        return sLen;
    }
    
    /*****************************************************************************
     * Name : printCmd
     * Purpose : Print the Command section of an EGNOS message
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :
     *  *************************************************************************/
    public void printCmd() {
        System.out.println();
        System.out.println("***COMMAND***");
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
        System.out.printf("commandType = %02X \n",this.commandType);
        System.out.printf("fileCode = %02X \n",this.fileCode);
        System.out.printf("commandParameter: ");
        for (int i = 0; i < this.commandParameter.length; i++) {
            System.out.printf("%02X ",this.commandParameter[i]);
        }
        System.out.println("\n");
    }
}

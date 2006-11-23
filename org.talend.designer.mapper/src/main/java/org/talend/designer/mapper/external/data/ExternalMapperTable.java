// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.mapper.external.data;

import java.io.Serializable;
import java.util.List;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class ExternalMapperTable implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8768004929161302382L;

    private List<ExternalMapperTableEntry> constraintTableEntries;

    private List<ExternalMapperTableEntry> metadataTableEntries;

    private String name;

    private boolean minimized;

    /**
     * Used only for outputs.
     */
    private boolean reject;

    /**
     * Used only for outputs.
     */
    private boolean rejectInnerJoin;
    
    /**
     * Used only for lookup inputs.
     * Reject main row if this lookup row doesn't exist.
     */
    private boolean innerJoin;
    
    public String getName() {
        return this.name;
    }

    public void setName(String medataTableName) {
        this.name = medataTableName;
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public List<ExternalMapperTableEntry> getMetadataTableEntries() {
        return this.metadataTableEntries;
    }

    public void setMetadataTableEntries(List<ExternalMapperTableEntry> tableEntries) {
        this.metadataTableEntries = tableEntries;
    }

    public List<ExternalMapperTableEntry> getConstraintTableEntries() {
        return this.constraintTableEntries;
    }

    public void setConstraintTableEntries(List<ExternalMapperTableEntry> constraintTableEntries) {
        this.constraintTableEntries = constraintTableEntries;
    }

    /**
     * 
     * used only for outputs.
     * 
     * @return
     */
    public boolean isReject() {
        return this.reject;
    }

    /**
     * 
     * used only for outputs.
     * 
     * @param reject
     */
    public void setReject(boolean reject) {
        this.reject = reject;
    }

    /**
     * Getter for rejectInnerJoin.
     * @return the rejectInnerJoin
     */
    public boolean isRejectInnerJoin() {
        return this.rejectInnerJoin;
    }

    /**
     * Sets the rejectInnerJoin.
     * @param rejectInnerJoin the rejectInnerJoin to set
     */
    public void setRejectInnerJoin(boolean rejectInnerJoin) {
        this.rejectInnerJoin = rejectInnerJoin;
    }

    /**
     * Getter for innerJoin.
     * @return the innerJoin
     */
    public boolean isInnerJoin() {
        return this.innerJoin;
    }

    
    /**
     * Sets the innerJoin.
     * @param innerJoin the innerJoin to set
     */
    public void setInnerJoin(boolean innerJoin) {
        this.innerJoin = innerJoin;
    }

    
    
}

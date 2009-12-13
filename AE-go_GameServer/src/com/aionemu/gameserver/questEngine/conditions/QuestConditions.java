//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.12.11 at 02:51:34 DU CET 
//


package com.aionemu.gameserver.questEngine.conditions;


import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.ConditionUnionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author MrPoke
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestConditions", propOrder = {
    "questConditions"
})
public class QuestConditions {

    @XmlElements({
        @XmlElement(name = "pc_level", type = PcLevelCondition.class),
        @XmlElement(name = "quest_var", type = QuestVarCondition.class),
        @XmlElement(name = "pc_inventory", type = PcInventoryCondition.class),
        @XmlElement(name = "dialog_id", type = DialogIdCondition.class),
        @XmlElement(name = "quest_status", type = QuestStatusCondition.class),
        @XmlElement(name = "npc_id", type = NpcIdCondition.class)
    })
    protected List<QuestCondition> questConditions;
    @XmlAttribute(required = true)
    protected ConditionUnionType operate;

	public boolean checkConditionOfSet(QuestEnv env)
	{
		boolean inCondition = (operate == ConditionUnionType.AND);
		for (QuestCondition cond : questConditions)
		{
			boolean bCond = cond.doCheck(env);
			switch (operate)
			{
				case AND:
					if (!bCond) return false;
					inCondition = inCondition && bCond;
					break;
				case OR:
					if (bCond) return true;
					inCondition = inCondition || bCond;
					break;
			}
		}
		return inCondition;
	}
}

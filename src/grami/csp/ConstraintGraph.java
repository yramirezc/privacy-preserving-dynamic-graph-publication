/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package grami.csp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import grami.dataStructures.ConnectedComponent;
import grami.dataStructures.GraMiGraph;
import grami.dataStructures.Query;
import grami.dataStructures.myNode;
import grami.pruning.SPpruner;

public class ConstraintGraph 
{
	private Variable[] variables;
	private Query qry;
	
	
	
	public ConstraintGraph(GraMiGraph graph,Query qry,HashMap<Integer, HashSet<Integer>> nonCandidates) 
	{
		this.qry=qry;		
		SPpruner sp = new SPpruner();
		sp.getPrunedLists(graph, qry,nonCandidates);
		variables= sp.getVariables();
	}
	
	public Query getQuery()
	{
		return qry;
	}

	public Variable[] getVariables() {
		return variables;
	}

	
}

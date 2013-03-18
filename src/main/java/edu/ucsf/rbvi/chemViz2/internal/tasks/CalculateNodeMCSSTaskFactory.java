package edu.ucsf.rbvi.chemViz2.internal.tasks;

import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.chemViz2.internal.model.ChemInfoSettings;

public class CalculateNodeMCSSTaskFactory extends ChemVizAbstractTaskFactory 
                                          implements NetworkTaskFactory, NodeViewTaskFactory {
	ChemInfoSettings settings = null;
	Scope scope;
	CyGroupManager groupManager;
	CyGroupFactory groupFactory;
	boolean showResult = false;
	boolean group = false;

	public CalculateNodeMCSSTaskFactory(ChemInfoSettings settings, CyGroupManager groupManager, CyGroupFactory groupFactory,
	                                    boolean showResult, boolean group, Scope scope) {
		this.settings = settings;
		this.scope = scope;
		this.groupManager = groupManager;
		this.groupFactory = groupFactory;
		this.showResult = showResult;
		this.group = group;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetwork network) {
		if (scope == Scope.ALLNODES && settings.hasNodeCompounds(network.getNodeList()))
			return true;

		if (scope == Scope.SELECTEDNODES)
			return selectedNodesReady(network);

		return false;
	}

	public boolean isReady(View<CyNode> nView, CyNetworkView netView) {
		if (nView != null && settings.hasNodeCompounds(Collections.singletonList(nView.getModel())))
			return true;

		return selectedNodesReady(netView.getModel());
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		if (scope == Scope.ALLNODES)
			return new TaskIterator(new CalculateMCSSTask(network, network.getNodeList(), settings, 
			                                              groupManager, groupFactory, showResult, group));
		else {
			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
			return new TaskIterator(new CalculateMCSSTask(network, selectedNodes, settings, 
			                                              groupManager, groupFactory, showResult, group));
		}
	}

	public TaskIterator createTaskIterator(View<CyNode> nView, CyNetworkView netView) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
		if (selectedNodes == null || selectedNodes.size() == 0)
			selectedNodes = Collections.singletonList(nView.getModel());

		return new TaskIterator(new CalculateMCSSTask(netView.getModel(), selectedNodes, settings, 
		                                           groupManager, groupFactory, showResult, group));
	}

	private boolean selectedNodesReady(CyNetwork network) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		if (selectedNodes != null && selectedNodes.size() > 0) {
			if (settings.hasNodeCompounds(selectedNodes))
				return true;
		}
		return false;
	}
}

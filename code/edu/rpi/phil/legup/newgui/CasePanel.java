package edu.rpi.phil.legup.newgui;

import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import edu.rpi.phil.legup.BoardState;
import edu.rpi.phil.legup.CaseRule;
import edu.rpi.phil.legup.Legup;
import edu.rpi.phil.legup.Selection;

/**
 * Provides a user interface for users to provide case rule justifications
 *
 */
public class CasePanel extends JustificationPanel
{
	private static final long serialVersionUID = -2304281047341398965L;

	protected final ImageIcon icon = new ImageIcon("images/Case Rules.gif");
	protected final String name = "Case Rules";
	protected final String toolTip = "Case Rules";

	private Vector<CaseRule> caseRules = null;

	private CaseRule defaultApplication; //NEEDED! Not yet reimplmented!

	/**
	 * Create a new ContradictionPanel
	 */
	CasePanel(JustificationFrame jf)
	{
		this.parentFrame = jf;
		setLayout(new FlowLayout());
	}

	/**
	 * set the case rules displayed by this case rule panel
	 * @param caseRules the vector of CaseRules
	 */
	public void setCaseRules(Vector<CaseRule> caseRules)
	{
		this.caseRules = caseRules;
		clearButtons();

		buttons = new JToggleButton[caseRules.size()];

		for (int x = 0; x < caseRules.size(); ++x)
		{
			CaseRule c = caseRules.get(x);
			buttons[x] = new JToggleButton(c.getImageIcon());
			this.parentFrame.getButtonGroup().add(buttons[x]);

			buttons[x].setToolTipText(c.toString());
			buttons[x].addActionListener(this);
			add(buttons[x]);
		}

		revalidate();
	}

	/**
	 * Check if the given case Rule can be applied to current board state
	 * @param c the case rule to be applied
	 */
	private void checkCaseRule(CaseRule c)
	{
		Selection sel = Legup.getInstance().getSelections().getFirstSelection();

		if (sel.isTransition())
		{
			BoardState state = sel.getState();

			// Update: Check only if immediate feedback enabled
			state.setCaseSplitJustification(c);
			String error = c.checkCaseRule(state);
			JustificationFrame.justificationApplied(state, c);
			parentFrame.resetJustificationButtons();

			if (error == null && LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK))
				parentFrame.setStatus(true,"The case rule is applied correctly!");
			else if (LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK)) parentFrame.setStatus(false, error);
		}
		else
		{
			parentFrame.resetJustificationButtons();
			parentFrame.setStatus(false, "Case Rules can only be applied to transitions, not states.");
			sel.getState().setJustification(null);
		}

		//parent.rep
	}


	/**
	 * Depresses the current rule button for user display
	 * @param c Rule to be pressed
	 * @return Whether or not the rule exists
	 */
	public boolean setCaseRule(CaseRule c)
	{
		for (int x = 0; x < caseRules.size(); ++x)
		{
			if (caseRules.get(x).equals(c))
			{
				buttons[x].setSelected(true);
				checkCaseRule(c);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void checkJustification(int button)
	{
		checkCaseRule(caseRules.get(button));
	}

	@Override
	protected void doDefaultApplication(int index, BoardState state)
	{
		//We set the current default application so we know which to apply for later
		CaseRule r = (CaseRule)caseRules.get(index);
		boolean legal = r.startDefaultApplication(state);

		if (!legal)
			parentFrame.setStatus(false, "There is not legal default application that can be applied.");
		else
		{
			parentFrame.setStatus(true, r.getApplicationText());
			Legup.getInstance().getPuzzleModule().defaultApplication = r;
			this.defaultApplication = r;
		}
	}
}
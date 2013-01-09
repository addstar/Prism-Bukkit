package me.botsko.prism.appliers;

import java.util.ArrayList;

import me.botsko.prism.events.containers.BlockStateChange;

public class ApplierResult {
	
	/**
	 * 
	 */
	protected int changes_applied = 0;
	
	/**
	 * 
	 */
	protected int changes_skipped = 0;
	
	/**
	 * 
	 */
	protected boolean is_preview;
	
	/**
	 * 
	 */
	protected ArrayList<BlockStateChange> blockStateChanges = new ArrayList<BlockStateChange>();
	
	
	/**
	 * 
	 * @param changes_applied
	 * @param changes_skipped
	 * @param messages
	 */
	public ApplierResult( boolean is_preview, int changes_applied, int changes_skipped, ArrayList<BlockStateChange> blockStateChanges ){
		this.changes_applied = changes_applied;
		this.changes_skipped = changes_skipped;
		this.is_preview = is_preview;
		this.blockStateChanges = blockStateChanges;
	}


	/**
	 * @return the changes_applied
	 */
	public int getChanges_applied() {
		return changes_applied;
	}


	/**
	 * @return the changes_skipped
	 */
	public int getChanges_skipped() {
		return changes_skipped;
	}


	/**
	 * @return the is_preview
	 */
	public boolean isPreview() {
		return is_preview;
	}


	/**
	 * @return the undo
	 */
	public ArrayList<BlockStateChange> getBlockStateChanges(){
		return blockStateChanges;
	}
}
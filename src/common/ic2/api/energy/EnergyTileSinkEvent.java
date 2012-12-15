package ic2.api.energy;

import ic2.api.IEnergySink;

/**
 * Event announcing an energy pull operation.
 *
 * This event notifies subscribers of energy sinks trying to pull energy from
 * an energy grid.
 *
 * IC2 currently doesn't use this mechanism, it's pushing energy directly from
 * the energy sources to the energy sinks. The event is implemented anyway to
 * allow different energy grids based on pulling energy to be used.
 *
 * The amount field indicates the maximum amount of energy which can fit into
 * the energy sink's input energy buffer. You can send energy to the sink by
 * calling IEnergySink.injectEnergy. Substract the amount you provided from the
 * 'amount' field to allow other event subscribers to send the appropriate
 * remaining amount of energy afterwards.
 *
 * The IEnergySink posting this event should not use amount to determine how
 * much has been provided, injectEnergy() has to do the associated processing.
 */
public class EnergyTileSinkEvent extends EnergyTileEvent {
	/**
	 * Maximum amount of energy accepted by the energy sink.
	 *
	 * amount needs to be adjusted to show the remaining energy to be accepted.
	 */
	public int amount;
	
	public EnergyTileSinkEvent(IEnergySink energySink, int amount) {
		super(energySink);
		
		this.amount = amount;
	}
}


package conquest.bot.playground;

import conquest.bot.state.AttackCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleResult {
    private double probability;
    private List<AttackCommand> resultCommand;

    public BattleResult(double probability, AttackCommand resultCommand) {
        this.probability = probability;
        this.resultCommand = Collections.singletonList(resultCommand);
    }

    public BattleResult(double probability, List<AttackCommand> resultCommand) {
        this.probability = probability;
        this.resultCommand = new ArrayList<>(resultCommand);
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<AttackCommand> getResultCommand() {
        return resultCommand;
    }

    public void setResultCommand(List<AttackCommand> resultCommand) {
        this.resultCommand = resultCommand;
    }
}

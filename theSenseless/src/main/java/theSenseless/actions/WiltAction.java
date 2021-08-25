package theSenseless.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import theSenseless.SenselessMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WiltAction extends AbstractGameAction {
    public static final Logger logger = LogManager.getLogger(SenselessMod.class.getName());

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("DiscardPileToTopOfDeckAction");

    public static final String[] TEXT = uiStrings.TEXT;

    private AbstractPlayer p;
    private CardGroup cardGroup;
    private CardGroup pickGroup;

    public WiltAction(String cardGroup, boolean ignoreCost) {
        this.p = AbstractDungeon.player;
        switch (cardGroup)
        {
            case "draw": this.cardGroup = this.p.drawPile; break;
            case "discard": this.cardGroup = this.p.discardPile; break;
            case "exhaust": this.cardGroup = this.p.exhaustPile; break;
        }

        pickGroup = new CardGroup(this.cardGroup.type);
        for (AbstractCard c : this.cardGroup.group)
        {
            logger.info("Sorting Wilt Deck. Cost: " + c.cost + " <= Energy: " + (EnergyPanel.totalCount - 1));
            if (c.cost <= EnergyPanel.totalCount - 1 || ignoreCost == true)
            {
                pickGroup.addToRandomSpot(c);
            }
        }

        this.actionType = AbstractGameAction.ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    public WiltAction(boolean ignoreCost)
    {
        this("discard",ignoreCost);
    }

    public WiltAction()
    {
        this("discard",false);
    }

    public void update() {
        //If the battle is ending, end action.
        if (AbstractDungeon.getCurrRoom().isBattleEnding()) {
            this.isDone = true;
            return;
        }
        // ???
        if (this.duration == Settings.ACTION_DUR_FAST) {
            //If the discard pile is empty, end action.
            if (pickGroup.isEmpty()) {
                this.isDone = true;
                return;
            } 
            //If the discard pile is only 1, automatically play card.
            if (pickGroup.size() == 1) {
                AbstractCard tmp = cardGroup.getTopCard();
                Wilt(tmp);
            } 
            //???
            if (pickGroup.group.size() > this.amount) {
                AbstractDungeon.gridSelectScreen.open(pickGroup, 1, "Choose a card to play and exhaust.", false, false, false, false);
                tickDuration();
                return;
            } 
        } 
        //If the cards are selected, do action.
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
                Wilt(c);
            } 
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            AbstractDungeon.player.hand.refreshHandLayout();
        } 
        tickDuration();
    }

    public void Wilt(AbstractCard c)
    {
        c.exhaust = true;
        cardGroup.group.remove(c);
        (AbstractDungeon.getCurrRoom()).souls.remove(c);
        addToBot((AbstractGameAction)new NewQueueCardAction(c, true, false, true));
        p.loseEnergy(c.cost);
    }
}


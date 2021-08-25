package theSenseless.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;

public class WiltAction extends AbstractGameAction {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("DiscardPileToTopOfDeckAction");

    public static final String[] TEXT = uiStrings.TEXT;

    private AbstractPlayer p;
    private CardGroup cardGroup;

    public WiltAction(String cardGroup) {
        this.p = AbstractDungeon.player;
        switch (cardGroup)
        {
            case "draw": this.cardGroup = this.p.drawPile; break;
            case "discard": this.cardGroup = this.p.discardPile; break;
            case "exhaust": this.cardGroup = this.p.exhaustPile; break;
        }
        this.actionType = AbstractGameAction.ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    public WiltAction()
    {
        this("discard");
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
            if (cardGroup.isEmpty()) {
                this.isDone = true;
                return;
            } 
            //If the discard pile is only 1, automatically play card.
            if (cardGroup.size() == 1) {
                AbstractCard tmp = cardGroup.getTopCard();
                Wilt(tmp);
            } 
            //???
            if (cardGroup.group.size() > this.amount) {
                AbstractDungeon.gridSelectScreen.open(cardGroup, 1, TEXT[0], false, false, false, false);
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
    }
}


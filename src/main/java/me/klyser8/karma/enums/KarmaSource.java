package me.klyser8.karma.enums;

public enum KarmaSource {
    //When karma is changed via a command.
    COMMAND,
    //When karma is changed via a mob's death
    MOB,
    //When karma is changed via a mob getting hit.
    ATTACK,
    //When karma is changed via a player being hit/killed.
    PLAYER,
    //When karma is changed passively.
    PASSIVE,
    //When karma is changed when an advancement is completed.
    ADVANCEMENT,
    //When karma is changed when a block is placed or destroyed.
    BLOCK,
    //When karma is changed via a trade.
    TRADE,
    //When karma is changed when taming an animal.
    TAME,
    //When karma is changed via feeding an animal.
    FEED,
    //When karma is changed via eating.
    FOOD,
    //When karma is changed via server voting.
    VOTING,
    //When karma is changed via messages.
    CHAT,
    //When karma is changed by other, unlisted means.
    MISC
}

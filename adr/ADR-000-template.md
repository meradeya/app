# Title

[//]: # ( Template version: 1.0 - Update this version when the template itself changes. Do not include the template version in the final ADR.)

- Status: Proposed | Accepted | Rejected | Superseded by [ADR #] | Supersedes [ADR #]
- Author: Who has proposed this decision

## Context and Problem Statement

Why was this decision necessary?

State the context and the problem that made this decision necessary.
Don't include too much detail. An ADR is not meant to be a full solution design. Keep records concise, assertive,
and factual.

## Decision Outcome

What was decided and why?

Focus on WHY the option was chosen, not on HOW it was implemented.

## Alternatives Considered

What alternatives were evaluated but rejected in the end?

State the reasons for the rejection. If no alternatives were evaluated, leave "None Explored."

* Option 1 - why rejected
* Option 2 - why rejected

## Consequences

What trade-offs were made, and what does this mean going forward?

State behavioral and architectural trade-offs only.

**Positive:**

- …

**Negative:**

- …

### ADR Checklist

> After verifying every item below, remove this section and everything under it in the final ADR, before committing it.

#### Scope

* ADR captures an architecturally significant decision. Anything else is not expected to be present in the ADR.
* ADR does not document implementation details, class designs, method choices, configuration values, or any other
  decision that can be changed without architectural impact.

#### Process

* ADR is included in the same PR as the code or change that implements the decision.
* ADR has been reviewed with the team - either during the PR code review or in a short dedicated meeting - before
  being merged.

#### Content

* ADR is short; at most ~100 lines - if it is longer, trim or move detail elsewhere.
* ADR explains WHY a decision was made, not HOW it was implemented.
* No class names, method names, field names, or configuration values appear in the text.


### Using AI to generate ADR

If you use AI to create the ADR, the quality and correctness of the output is entirely your responsibility.
AI can only combine and format what you give it. It cannot know what you discussed with your team, what you tried and
discarded, or why you ultimately chose one direction over another. If you do not supply that reasoning, the AI will
either omit it or invent it.

Example prompt to use with AI:

```  
I need to write an ADR following the project template.  
Below is raw context (documents, presentations, commit hashes, diffs, meeting notes, email chains with discussions).  
Extract the problem, the decision, the reasoning, the alternatives, and the trade-offs from this material.  
Then combine them into a well-formed ADR and verify every rule listed in the AI Instructions of the template.
    
CONTEXT:
<paste everything here: documents, presentations, commit hashes, diffs, meeting notes, email chains with discussions>  

```

<!--
AI Instructions:

When generating or improving an ADR based on this template, strictly follow every rule in the checklist.
After generating ADR, you must review it according to the checklist. If any of the rules is not respected, improve the ADR until
the checklist is not violated.

- If a sentence answers "how is this built?" rather than "why was this direction chosen?", reformulate or remove it.
- Implementation details that leak into the text must be replaced with the architectural concern they represent.

- List only alternatives that were provided by the prompt, comments, context.
- If you detect that the input info does not contain any mentions of the alternatives, you must not generate alternatives
by yourself. In this case, write exactly: "None explored."
- For each alternative state *why it was rejected* at the architectural level - not how it works internally.
- Do not describe implementation mechanics of the rejected options.

- State behavioral and architectural trade-offs only.
- If a trade-off is unknown or not yet observable, write exactly: "Not identified."
- Do not list implementation tasks, migration steps, or class-level changes as consequences.

- The finished ADR must be at most ~100 lines.
- Write in concise, assertive, factual language. Avoid wordy sentences.
- Remove this entire checklist section (### ADR Checklist and everything under it) from the final ADR.
-->

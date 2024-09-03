# query-generator

Generate an Apache Solr Boolean Query in a structured manner.

Using these classes allows queries to be manipulated in various ways before calling toString to generate an Apache Solr Boolean Query.

If you have ever built an Apache Solr query using string manipulation and then you decide to change something such as the boost of a sub-query it requires string manipulation methods such as using regex to find the part of the query you wish modified.

Using the classes in this repository can simplify query generation and modification.

The classes you should experiment with are Term and TermGroup.

I recommend a study of the unit tests.

Term is a unit of search. It is composed of two elements, the value of the word as a string and the name of the field.
For example:

    +title:"pink panther"~1
    title:("pink panther" "treasure island")
    year:[1950 TO 1960]^=2
    year:1953^0.5

    Instantiate a Term and set the values and call toString to get a string that can be used in a Standard Solr Query.
    Term term = new Term("pink panther").withBoost(1.5f);
    term. toString()

       Output: "pink panther"^1.5
  
       Term term = new Term("title", "pink panther").withBoost(1.5f);
       term. toString()
 
       Output: title:"pink panther"^1.5



TermGroup aggregates Terms and other TermGroups to form complex queries that can be used in a Standard Solr Query

Example:

    TermGroup group = new TermGroup().with(Occur. MUST).withBoost(1.4f);
    group.addTerm(new Term("foo", "bar").withProximity(1));

    String query = group.toString();
 
    Output: +( foo:bar~1 )^1.4

Example:

    TermGroup group = new TermGroup().withConstantScore(5.0f);
    group.addTerm(new Term("foo", "bar").withProximity(1));

    String query = group.toString();
 
    Output: ( foo:bar~1 )^=5

Instead of using string manipulation to create complex query strings the TermGroup allows complex queries to be built inside an object model that can be more easily changed.

If you need to generate a query like this:

    +(
        (
            title:"Grand Illusion"~1
            title:"Paradise Theatre"~1
        )^0.3
        (
            title:"Night At The Opera"~1
            title:"News Of The World"~1
        )^0.3
        (
            title:"Van Halen"~1
            title:1984~1
        )^0.3
    )


The code to do so is as simple this:

      TermGroup group = new TermGroup().with(Occur. MUST);
 
      TermGroup favoriteStyx = group.addGroup().withBoost(0.3f);
      TermGroup favoriteQueen = group.addGroup().withBoost(0.3f);
      TermGroup favoriteVanHalen = group.addGroup().withBoost(0.3f);
 
      favoriteStyx.addTerm(new Term("title","Grand Illusion").with(Occur.SHOULD).withProximity(1));
      favoriteStyx.addTerm(new Term("title","Paradise Theatre").with(Occur.SHOULD).withProximity(1));
 
      favoriteQueen.addTerm(new Term("title","Night At The Opera").with(Occur.SHOULD).withProximity(1));
      favoriteQueen.addTerm(new Term("title","News Of The World").with(Occur.SHOULD).withProximity(1));
 
      favoriteVanHalen.addTerm(new Term("title","Van Halen").with(Occur.SHOULD).withProximity(1));
      favoriteVanHalen.addTerm(new Term("title","1984").with(Occur.SHOULD).withProximity(1));


Here is a very simplified and contrived example of some of the things you can do.


    public TermGroup generate(SearchRequest request) {
        TermGroup group = new TermGroup().withLabel("FULL_REQUEST");
        TermGroup groupA = new TermGroup().withLabel("FIRST_NAMES");
        group.addGroup(groupA);
        groupA.addTerm(new Term("firstName", "jeff"));
        groupA.addTerm(new Term("firstName", "jeffrey"));
        groupA.addTerm(new Term("firstName", "geoffrey"));
    
        TermGroup groupB = new TermGroup().withLabel("MIDDLE_NAMES");
        group.addGroup(groupB);
        groupB.addTerm(new Term("middleName", "john"));
        groupB.addTerm(new Term("middleName", "jon"));
        groupB.addTerm(new Term("middleName", "sean"));
    
    
        TermGroup groupC = new TermGroup().withLabel("LAST_NAMES");
        group.addGroup(groupC);
    
        groupC.addTerm(new Term("lastName", "smith"));
        groupC.addTerm(new Term("lastName", "smythe"));
        groupC.addTerm(new Term("lastName", "schmidt"));
    
        return group;
    }


    public void contrivedExample() {
        SearchRequest request = new SearchRequest();
        TermGroup requestGroup = generate(request);
    
        String prettyQuery = requestGroup.prettyPrint(true, "", "  ", "\n");
    
        //At least one match on the middle names is wanted
        List<TermGroup> middleNames = requestGroup.findByLabel("MIDDLE_NAMES");
        middleNames.get(0).setOccur(Occur.MUST);
    
        prettyQuery = requestGroup.prettyPrint(true, "", "  ", "\n");
    
        //We want to boost last names
        List<TermGroup> lastNames = requestGroup.findByLabel("LAST_NAMES");
        lastNames.get(0).setBoost(2.0f);
    
        prettyQuery = requestGroup.prettyPrint(true, "", "  ", "\n");
    
        //We want to weight the first names
        List<TermGroup> firstNames = requestGroup.findByLabel("FIRST_NAMES");
        firstNames.get(0).setBoost( 1.0f / firstNames.get(0).getTerms().size());
    
        prettyQuery = requestGroup.prettyPrint(true, "", "  ", "\n");
    
    
    }

Here are the resulting queries in order of the code:

The original:

    /* FULL_REQUEST */
    (
        /* FIRST_NAMES */
        (
            firstName:jeff
            firstName:jeffrey
            firstName:geoffrey
        )
        /* MIDDLE_NAMES */
        (
            middleName:john
            middleName:jon
            middleName:sean
        )
        /* LASTNAMES */
        (
            lastName:smith
            lastName:smythe
            lastName:schmidt
        )
    )



Query where there must be at least one match on Middle Name:


    /* FULL_REQUEST */
    (
        /* FIRST_NAMES */
        (
            firstName:jeff
            firstName:jeffrey
            firstName:geoffrey
        )
        /* MIDDLE_NAMES */
        +(
            middleName:john
            middleName:jon
            middleName:sean
        )
        /* LASTNAMES */
        (
            lastName:smith
            lastName:smythe
            lastName:schmidt
        )
    )

Query that boosts the score of the Last Names

    /* FULL_REQUEST */
    (
        /* FIRST_NAMES */
        (
            firstName:jeff
            firstName:jeffrey
            firstName:geoffrey
            )
        /* MIDDLE_NAMES */
        +(
            middleName:john
            middleName:jon
            middleName:sean
        )
        /* LAST_NAMES */
        (
            lastName:smith
            lastName:smythe
            lastName:schmidt
        )^2
    )


Query with added weight to the first names:

    /* FULL_REQUEST */
    (
        /* FIRST_NAMES */
        (
            firstName:jeff
            firstName:jeffrey
            firstName:geoffrey
        )^0.3333
        /* MIDDLE_NAMES */
        +(
            middleName:john
            middleName:jon
            middleName:sean
        )
        /* LAST_NAMES */
        (
            lastName:smith
            lastName:smythe
            lastName:schmidt
        )^2
    )



By using a structured query builder you can:

* Add or remove terms
* Add or remove a sub query (a group)
* Surround a group inside another
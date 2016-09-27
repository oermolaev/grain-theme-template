package com.sysgears.theme

import com.sysgears.grain.taglib.Site

/**
 * Change pages urls and extend models.
 */
class ResourceMapper {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    public ResourceMapper(Site site) {
        this.site = site
    }

    /**
     * This closure is used to transform page URLs and page data models.
     */
    def map = { resources ->

        def refinedResources = resources.findResults(filterPublished).collect { Map resource ->
            fillRelatedPosts << fillDates << resource
        }

        refinedResources
    }

    /**
     * Excludes resources with published property set to false,
     * unless it is allowed to show unpublished resources in SiteConfig.
     */
    private def filterPublished = { Map it ->
        (it.published != false || site.show_unpublished) ? it : null
    }

    /**
     * Fills in page `date` and `updated` fields 
     */
    private def fillDates = { Map it ->
        def update = [date: it.date ? Date.parse(site.datetime_format, it.date) : new Date(it.dateCreated as Long),
                updated: it.updated ? Date.parse(site.datetime_format, it.updated) : new Date(it.lastUpdated as Long)]
        it + update
    }

    /**
     * Fills in page 'related' field which may contain related posts.
     *
     * Related posts here are pages under /posts/ location which have at least one
     * common entry in the "categories" list property.
     */
    private def fillRelatedPosts = { Map it ->
        isPost(it) ?
            it + [related: getPosts().grep { post -> !post.categories.disjoint(it.categories)}] :
            it
    }

    /**
     * Retrieves all the blog post pages of the website.
     *
     * @return website blog posts collection
     */
    private List<Map> getPosts() {
        site.resources.findAll isPost
    }

    /**
     * Checks whether provided resource is a blog post page or not.
     */
    private def isPost = { Map it ->
        it.type == 'page' && it.location.startsWith("/posts/")
    }
}

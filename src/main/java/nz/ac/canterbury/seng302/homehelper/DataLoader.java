package nz.ac.canterbury.seng302.homehelper;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Component
@Profile({"!test"})
public class DataLoader implements CommandLineRunner {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RenovationRecordService renovationRecordService;
    private final JobService jobService;
    private final TagService tagService;
    private final ExpenseService expenseService;
    private final QuoteService quoteService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public DataLoader(UserService userService, RenovationRecordService renovationRecordService, JobService jobService,
                      TagService tagService, ExpenseService expenseService, QuoteService quoteService, UserRepository userRepository, JobRepository jobRepository) {
        this.userService = userService;
        this.renovationRecordService = renovationRecordService;
        this.jobService = jobService;
        this.tagService = tagService;
        this.expenseService = expenseService;
        this.quoteService = quoteService;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) throws ParseException {
        Random rand = new Random();

        if (userService.getUser("john@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("John", "Doe", "john@example.com", hashedPassword, null, null);
            newUser.grantAuthority("ROLE_USER");
            newUser.setStreetAddress("123 ABC Street");
            newUser.setSuburb("Auckland Central");
            newUser.setCity("Auckland");
            newUser.setPostcode("1010");
            newUser.setCountry("New Zealand");
            newUser.setRecentJobs(new ArrayList<>(List.of(1L, 2L, 3L, 4L)));
            userService.addUser(newUser);
        }
        if (userService.getUser("jane@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Jane", "Doe", "jane@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("123 ABC Street");
            newUser.setSuburb("Epsom");
            newUser.setCity("Auckland");
            newUser.setPostcode("1010");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            userService.addUser(newUser);
        }
        if (userService.getUser("calan@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Calan", "Meechang", "calan@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("123 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        if (userService.getUser("luke@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Luke", "Burton", "luke@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("456 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        if (userService.getUser("fergus@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Fergus", "Ord", "fergus@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("456 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        if (userService.getUser("dury@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Dury", "Kim", "dury@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("456 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        if (userService.getUser("alex@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Alex", "Cheals", "alex@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("456 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        if (userService.getUser("morgan@example.com") == null) {
            String hashedPassword = passwordEncoder.encode("P4$$word");
            User newUser = new User("Morgan", "Lee", "morgan@example.com", hashedPassword, null, null);
            newUser.setStreetAddress("456 ABC Street");
            newUser.setSuburb("Ilam");
            newUser.setCity("Christchurch");
            newUser.setPostcode("8041");
            newUser.setCountry("New Zealand");
            newUser.grantAuthority("ROLE_USER");
            addRatings(newUser);
            userService.addUser(newUser);
            newUser.setCreatedTimestamp(LocalDate.now().minusDays(3 + (new Random()).nextInt(1825)));
            userRepository.save(newUser);
        }
        User john = userService.getUser("john@example.com");
        User jane = userService.getUser("jane@example.com");
        User calan = userService.getUser("calan@example.com");
        User luke = userService.getUser("luke@example.com");
        User fergus = userService.getUser("fergus@example.com");
        User dury = userService.getUser("dury@example.com");
        User alex = userService.getUser("alex@example.com");
        User morgan = userService.getUser("morgan@example.com");

        if (renovationRecordService.getRenovationRecordsByOwner("john@example.com").isEmpty()) {
            List<Room> rooms = List.of(new Room("Kitchen"), new Room("Bathroom"), new Room("Bedroom"));
            RenovationRecord renovationRecord = new RenovationRecord("Example Renovation Record", "Example Renovation Record", rooms, "john@example.com" );
            renovationRecord.setStreetAddress("90 Ilam Road");
            renovationRecord.setSuburb("Ilam");
            renovationRecord.setCity("Christchurch");
            renovationRecord.setPostcode("8041");
            renovationRecord.setCountry("New Zealand");
            RenovationRecord persistedRecord = renovationRecordService.addRenovationRecord(renovationRecord);

            List<Room> jERooms = List.of(new Room("031"), new Room("101"), new Room("110"), new Room("201"));
            RenovationRecord jERenovationRecord = new RenovationRecord("Jack Erskine", "Renovations for bathroom plumbing adding new hand towels. Upgrading heat in some tutorial rooms. Renovations for bathroom plumbing adding new hand towels. Upgrading heat in some tutorial rooms. Renovations for bathroom plumbing adding new hand towels. Upgrading heat in some tutorial rooms.", jERooms, "john@example.com" );
            List<Room> erRooms = List.of(new Room("031"), new Room("101"), new Room("110"), new Room("201"));
            RenovationRecord erRenovationRecord = new RenovationRecord("Ernest Rutherford", "we needed another building", erRooms, "john@example.com" );
            Tag plumbingTag = new Tag("plumbing");
            Tag heatingTag = new Tag("heating");
            Tag computerTag = new Tag("computer");
            Tag flooringTag = new Tag("flooring");
            Tag roofingTag = new Tag("roofing");
            Tag electricityTag = new Tag("electricity");
            erRenovationRecord.setIsPublic(true);
            jERenovationRecord.setIsPublic(true);
            jERenovationRecord.addTag(plumbingTag);
            jERenovationRecord.addTag(heatingTag);
            jERenovationRecord.addTag(computerTag);
            jERenovationRecord.addTag(flooringTag);
            jERenovationRecord.addTag(roofingTag);
            erRenovationRecord.addTag(electricityTag);
            plumbingTag.addRenovationRecord(jERenovationRecord);
            heatingTag.addRenovationRecord(jERenovationRecord);
            computerTag.addRenovationRecord(jERenovationRecord);
            flooringTag.addRenovationRecord(jERenovationRecord);
            roofingTag.addRenovationRecord(jERenovationRecord);
            electricityTag.addRenovationRecord(erRenovationRecord);
            tagService.addTag(plumbingTag);
            tagService.addTag(heatingTag);
            tagService.addTag(computerTag);
            tagService.addTag(flooringTag);
            tagService.addTag(roofingTag);
            tagService.addTag(electricityTag);
            erRenovationRecord = renovationRecordService.addRenovationRecord(erRenovationRecord);
            jERenovationRecord = renovationRecordService.addRenovationRecord(jERenovationRecord);

            // Different Locations
            RenovationRecord aucklandRenovation = new RenovationRecord("Auckland Epsom Renovation", "Seoul Chicken Epsom", Collections.emptyList(), "john@example.com" );
            aucklandRenovation.setStreetAddress("563 Manukau Road");
            aucklandRenovation.setSuburb("Epsom");
            aucklandRenovation.setCity("Auckland");
            aucklandRenovation.setPostcode("1023");
            aucklandRenovation.setCountry("New Zealand");
            Job jobAuckland = new Job("Epsom Job", "Epsom Example",
                    "31/12/2025", "13/10/2025");
            jobAuckland.setType("Carpentry");
            jobAuckland.setRenovationRecord(aucklandRenovation);
            jobAuckland.setIsPosted(true);
            renovationRecordService.addRenovationRecord(aucklandRenovation);
            jobService.addJob(jobAuckland);

            RenovationRecord aucklandCentralRenovation = new RenovationRecord("Auckland Central Renovation", "Tsujiri Auckland Central", Collections.emptyList(), "john@example.com" );
            aucklandCentralRenovation.setStreetAddress("10 Lorne Street");
            aucklandCentralRenovation.setSuburb("Auckland Central");
            aucklandCentralRenovation.setCity("Auckland");
            aucklandCentralRenovation.setPostcode("1010");
            aucklandCentralRenovation.setCountry("New Zealand");
            RenovationRecord centralPersisted = renovationRecordService.addRenovationRecord(aucklandCentralRenovation);
            Job jobAucklandCentral = new Job("Auckland Job", "Example Job Central",
                    "31/12/2025", "13/10/2025");
            jobAucklandCentral.setType("Carpentry");
            jobAucklandCentral.setRenovationRecord(centralPersisted);
            jobAucklandCentral.setIsPosted(true);
            jobService.addJob(jobAucklandCentral);

            RenovationRecord cambridgeRenovation = new RenovationRecord("Cambridge Renovation", "Mitre 10 MEGA Cambridge", Collections.emptyList(), "john@example.com" );
            cambridgeRenovation.setStreetAddress("1 Oliver Street");
            cambridgeRenovation.setSuburb("");
            cambridgeRenovation.setCity("Cambridge");
            cambridgeRenovation.setPostcode("3434");
            cambridgeRenovation.setCountry("New Zealand");
            RenovationRecord cambridgePersisted = renovationRecordService.addRenovationRecord(cambridgeRenovation);
            Job jobCambridge = new Job("Cambridge Job", "Cambridge Example",
                    "31/12/2025", "13/10/2025");
            jobCambridge.setType("Carpentry");
            jobCambridge.setRenovationRecord(cambridgePersisted);
            jobCambridge.setIsPosted(true);
            jobService.addJob(jobCambridge);

            // Jobs
            Job jobC = new Job("Example Job Carpentry", "Example Job Carpentry",
                    "13/10/2025", "13/10/2025");
            jobC.setType("Carpentry");
            jobC.setRenovationRecord(persistedRecord);
            jobC.setIsPosted(true);
            jobService.addJob(jobC);
            Job jobPl = new Job("Example Job Plumbing", "Example Job Plumbing",
                    "14/10/2025", "14/10/2025");
            jobPl.setType("Plumbing");
            jobPl.setRenovationRecord(persistedRecord);
            jobPl.setIsPosted(true);
            jobService.addJob(jobPl);
            Job jobPla = new Job("Example Job Plastering", "Example Job Plastering",
                    "15/10/2025", "15/10/2025");
            jobPla.setType("Plastering");
            jobPla.setRenovationRecord(persistedRecord);
            jobPla.setIsPosted(true);
            jobService.addJob(jobPla);
            Job jobP = new Job("Example Job Painting", "Example Job Painting",
                    "16/10/2025", "16/10/2025");
            jobP.setType("Painting");
            jobP.setRenovationRecord(persistedRecord);
            jobP.setIsPosted(true);
            jobService.addJob(jobP);
            Job jobI = new Job("Example Job Insulating", "Example Job Insulating",
                    "17/10/2025", "17/10/2025");
            jobI.setType("Insulating");
            jobI.setRenovationRecord(persistedRecord);
            jobI.setIsPosted(true);
            jobService.addJob(jobI);
            Job startJob = new Job("Start Date Job", "Start Date Job",
                    null, "10/10/2025");
            startJob.setRenovationRecord(erRenovationRecord);
            jobService.addJob(startJob);
            Job dueJob = new Job("Due Date Job", "Due Date Job",
                    "11/10/2025", null);
            dueJob.setRenovationRecord(erRenovationRecord);
            jobService.addJob(dueJob);

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            Date startDate = new Date();
            calendar.setTime(startDate);
            calendar.add(Calendar.DATE, 5);
            Date dueDate = calendar.getTime();
            int dateSeparatedInDays = 0;

            for (int i = 1; i <= 95; i++) {
                calendar.setTime(startDate);
                calendar.add(Calendar.DATE, dateSeparatedInDays);
                String startDateString = format.format(calendar.getTime());
                calendar.setTime(dueDate);
                calendar.add(Calendar.DATE, dateSeparatedInDays);
                String dueDateString = format.format(calendar.getTime());
                Job job = new Job("John Example Job "+i, "John Example Job "+i,
                        dueDateString, startDateString);
                job.setRenovationRecord(persistedRecord);
                job.setType("Carpentry");
                job.setIsPosted(true);
                job = jobService.addJob(job);
                dateSeparatedInDays += 4;


                Quote quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), "jane@example.com", "123456", "Quote");
                quote.setJob(job);
                quote.setUser(jane);
                quote.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "calan@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(calan);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "luke@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(luke);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "fergus@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(fergus);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "dury@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(dury);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "alex@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(alex);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "morgan@example.com", null, "I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you. I would like to work in your house. Please give me money and a job. I will help your home. PLEASE let me work for you.");
                quote.setJob(job);
                quote.setUser(morgan);
                quote.setStatus("Pending");
                quoteService.addQuote(quote);
            }

            for (int i = 1; i <= 200; i++) {
                List<Room> exampleRooms = List.of(new Room("Example Room 1"), new Room("Example Room 2"), new Room("Example Room 3"));
                RenovationRecord exampleRenovationRecord = new RenovationRecord("Example Renovation Record "+i, "Example Renovation Record "+i, exampleRooms, "john@example.com" );
                exampleRenovationRecord.setIsPublic(true);
                renovationRecordService.addRenovationRecord(exampleRenovationRecord);
            }

            Job job = jobService.getJobById(1);
            for (int i = 1; i <= 100; i++) {
                Expense expense = new Expense("25", "Example expense description " + (i), "Material", "19/07/2025");
                expense.setJob(job);
                expenseService.addExpense(expense);
            }

            Quote quote = new Quote("100", "24", "jane@example.com", "123456", "Quote");
            quote.setJob(jobService.getJobById(2));
            quote.setUser(jane);
            quoteService.addQuote(quote);

            Job janeJob = new Job("Example Jane Job", "desc", "13/09/2025", "15/09/2025");
            janeJob.setRenovationRecord(erRenovationRecord);

            Job jobJane = new Job("Example Job Jane", "Example Job Jane",
                    "13/10/2025", "13/10/2025");
            jobJane.setType("Carpentry");
            RenovationRecord recordJane = new RenovationRecord("janeRecord", "desc", List.of(), "jane@example.com");
            recordJane.setStreetAddress("123 ABC Street");
            recordJane.setSuburb("Epsom");
            recordJane.setCity("Auckland");
            recordJane.setPostcode("1010");
            recordJane.setCountry("New Zealand");
            recordJane.setLatitude(-43.52287339922578f);
            recordJane.setLongitude(172.62022729095878f);
            renovationRecordService.addRenovationRecord(recordJane);
            jobJane.setRenovationRecord(recordJane);
            jobJane.setIsPosted(true);
            jobService.addJob(jobJane);

            startDate = new Date();
            calendar.setTime(startDate);
            calendar.add(Calendar.DATE, 5);
            dueDate = calendar.getTime();
            dateSeparatedInDays = 0;
            int numberOfCompletedJobsInPortfolio = 4;
            int numberOfCompletedJobs = 20;
            Random random = new Random();
            LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 1; i <= 95; i++) {
                int separation = 2 + random.nextInt(13);
                dateSeparatedInDays += separation;
                LocalDate jobStart = start.plusDays(dateSeparatedInDays);
                int time = 3 + random.nextInt(5);
                LocalDate jobDue = jobStart.plusDays(time);
                LocalDate completionDate = jobDue.plusDays(random.nextInt(0, 8) - time);
                String startDateString = jobStart.format(formatter);
                String dueDateString = jobDue.format(formatter);

                job = new Job("Jane Example Job " + i, "Jane Example Job " + i,
                        dueDateString, startDateString);
                job.setRenovationRecord(recordJane);
                job.setType("Carpentry");
                job.setIsPosted(true);
                job.setStatus("Completed");
                job.setCompletedTimestamp(completionDate);

                job = jobService.addJob(job);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "john@example.com", "123456", "Quote");
                quote.setJob(job);
                quote.setUser(john);
                if (numberOfCompletedJobs > 0) {
                    quote.setStatus("Accepted");
                    numberOfCompletedJobs--;
                } else {
                    quote.setStatus("Pending");
                }
                quoteService.addQuote(quote);


                try {
                    if (numberOfCompletedJobsInPortfolio > 0) {
                        job.addPortfolioUser(john);
                        john.addPortfolioJob(job);
                        jobRepository.save(job);
                        userRepository.save(john);
                        numberOfCompletedJobsInPortfolio -= 1;
                    }
                } catch (Exception ignored) {}

                if (i < 60 && i > 30) {

                }

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "calan@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(calan);
                quote.setStatus(i < 60 && i > 36 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "luke@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(luke);
                quote.setStatus(i < 60 && i > 32 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "fergus@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(fergus);
                quote.setStatus(i < 60 && i > 23 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "dury@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(dury);
                quote.setStatus(i < 60 && i > 22 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "alex@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(alex);
                quote.setStatus(i < 60 && i > 46 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);

                quote = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24),
                        "morgan@example.com", null, "Quote");
                quote.setJob(job);
                quote.setUser(morgan);
                quote.setStatus(i < 60 && i > 38 ? "Accepted" : "Pending");
                quoteService.addQuote(quote);
            }

            for (int i = 1; i <= 200; i++) {
                Quote q2 = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), "jane@example.com", "123456", "Quote");
                q2.setJob(jobService.getJobById(2));
                q2.setUser(jane);
                q2.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quoteService.addQuote(q2);
            }

            //Add quote from new tradie to check rating multiple tradies
            if (userService.getUser("tradie@jack.com") == null) {
                String hashedPassword = passwordEncoder.encode("P4$$word");
                User newUser = new User("Tradie", "Jack", "tradie@jack.com", hashedPassword, null, null);
                newUser.setStreetAddress("123 ABC Street");
                newUser.setSuburb("Epsom");
                newUser.setCity("Auckland");
                newUser.setPostcode("1010");
                newUser.setCountry("New Zealand");
                newUser.grantAuthority("ROLE_USER");
                userService.addUser(newUser);
            }
            User jack = userService.getUser("tradie@jack.com");
            Quote jackQuote = new Quote("100", "12", "tradie@jack.com", "654321", "Quote");
            jackQuote.setJob(jobService.getJobById(2));
            jackQuote.setUser(jack);
            jackQuote.setStatus("Accepted");
            quoteService.addQuote(jackQuote);

        }
        try {if (userService.getUser("joan@example.com") == null) {
            john = userService.getUser("john@example.com");
            String hashedPassword = passwordEncoder.encode("P4$$word");

            User joan = new User("Joan", "Doe", "joan@example.com", hashedPassword, null, null);
            User jeff = new User("Jeff", "Doe", "jeff@example.com", hashedPassword, null, null);
            User jill = new User("Jill", "Doe", "jill@example.com", hashedPassword, null, null);
            User jebb = new User("Jebb", "Doe", "jebb@example.com", hashedPassword, null, null);

            joan.setLatitude(-43.530925f);joan.setLongitude(172.637024f);
            jeff.setLatitude(-43.516107f);jeff.setLongitude(172.571768f);
            jill.setLatitude(-43.524759f);jill.setLongitude(172.575681f);
            jebb.setLatitude(-43.531181f);jebb.setLongitude(172.596391f);

            List<User> renovators = List.of(joan, jeff, jill, jebb);
            for (User user : renovators) {

                user.grantAuthority("ROLE_USER");
                user.setStreetAddress("123 ABC Street");
                user.setSuburb("Auckland Central");
                user.setCity("Auckland");
                user.setPostcode("1010");
                user.setCountry("New Zealand");
                user.setRecentJobs(new ArrayList<>(List.of(1L, 2L, 3L, 4L)));
                userService.addUser(user);

                RenovationRecord record = new RenovationRecord(user.getFirstName() + " renovation", "desc", List.of(), user.getEmail());
                renovationRecordService.addRenovationRecord(record);
                Job job = new Job(user.getFirstName() + " job", "desc", "31/12/2025", "30/12/2025");
                job.setType("Carpentry");
                job.setIsPosted(true);
                Quote quote = new Quote("1", "1", "john@example.com", null, "d");
                quote.setUser(john);
                record.setLatitude(user.getLatitude());
                record.setLongitude(user.getLongitude());
                quote.setStatus("Accepted");
                quote.setJob(job);
                job.setStatus("Completed");
                job.setQuotes(List.of(quote));
                record.setJobs(List.of(job));
                job.setRenovationRecord(record);
                job.addPortfolioUser(user);
                john.addPortfolioJob(job);
                record.setStreetAddress("1 Oliver Street");
                record.setSuburb("");
                record.setCity("Cambridge");
                record.setPostcode("3434");
                record.setCountry("New Zealand");

                renovationRecordService.addRenovationRecord(record);
                jobService.addJob(job);
                quoteService.addQuote(quote);
                userService.addUser(user);
            }
            userService.addUser(john);
        }} catch (Exception ignored) {}
    }

    public static void addRatings(User user) {
        List<Rating> ratings = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < r.nextInt(10, 50); i++) {
            Rating rating = new Rating(r.nextInt(1, 6), user, null);
            ratings.add(rating);
        }
        user.setReceivedRatings(ratings);
    }
}
